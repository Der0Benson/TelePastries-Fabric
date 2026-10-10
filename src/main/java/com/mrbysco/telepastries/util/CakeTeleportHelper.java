package com.mrbysco.telepastries.util;

import com.mrbysco.telepastries.TelePastries;
import com.mrbysco.telepastries.blocks.cake.BlockCakeBase;
import com.mrbysco.telepastries.config.TeleConfig;
import com.mrbysco.telepastries.data.TelePastriesPlayerData;
import it.unimi.dsi.fastutil.longs.Long2BooleanArrayMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.EndPlatformFeature;
import org.jetbrains.annotations.Nullable;

public final class CakeTeleportHelper {
    private CakeTeleportHelper() {
    }

    public static TeleportTarget getCakeTeleportTarget(ServerWorld destination, Entity entity) {
        entity.fallDistance = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 200, false, false));
        }

        Bounds bounds = customBounds(destination).clamp(destination.getBottomY(), destination.getTopY() - 1);
        Long2BooleanArrayMap safetyCache = new Long2BooleanArrayMap();
        BlockPos saved = getDimensionPosition(entity, destination.getRegistryKey());
        if (saved != null && destination.getBlockState(saved.down()).isSolidBlock(destination, saved.down())
                && isPositionSafe(entity, destination, saved, safetyCache, bounds)) {
            return finish(destination, saved, entity);
        }

        if (destination.getRegistryKey().equals(World.END)) {
            BlockPos endSpawn = ServerWorld.END_SPAWN_POS;
            EndPlatformFeature.generate(destination, endSpawn.down(), true);
            return finish(destination, endSpawn, entity);
        }

        TeleportTarget searched = searchAroundAndDown(entity, destination, bounds, safetyCache);
        if (searched != null) return searched;

        BlockPos current = entity.getBlockPos();
        boolean currentInBounds = destination.getWorldBorder().contains(current)
                && bounds.min < current.getY() - 1
                && bounds.max > current.getY() + entity.getHeight() + 1;
        int highestFallbackY = Math.max(bounds.min, bounds.max - MathHelper.ceil(entity.getHeight()) - 1);
        int fallbackY = MathHelper.clamp(70, bounds.min, highestFallbackY);
        BlockPos teleportPos = currentInBounds ? current : new BlockPos(0, fallbackY, 0);
        buildSafetyCage(destination, teleportPos, entity);
        return finish(destination, teleportPos, entity);
    }

    @Nullable
    private static TeleportTarget searchAroundAndDown(Entity entity, ServerWorld destination, Bounds bounds,
                                                       Long2BooleanArrayMap safetyCache) {
        double scale = DimensionType.getCoordinateScaleFactor(
                entity.getWorld().getDimension(), destination.getDimension());
        BlockPos center = destination.getWorldBorder().clamp(
                entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        int top = Math.min(bounds.max, destination.getBottomY() + destination.getLogicalHeight() - 1);
        center = center.withY(top);

        for (BlockPos horizontal : BlockPos.iterateOutwards(center, 16, 0, 16)) {
            destination.getChunk(horizontal.getX() >> 4, horizontal.getZ() >> 4);
            int surface = destination.getTopY(Heightmap.Type.MOTION_BLOCKING,
                    horizontal.getX(), horizontal.getZ());
            for (int y = Math.min(top, surface); y > bounds.min; --y) {
                BlockPos check = new BlockPos(horizontal.getX(), y, horizontal.getZ());
                if (destination.getBlockState(check.down()).isSolidBlock(destination, check.down())
                        && isPositionSafe(entity, destination, check, safetyCache, bounds)) {
                    return finish(destination, check, entity);
                }
            }
        }
        return null;
    }

    private static boolean isPositionSafe(Entity entity, ServerWorld destination, BlockPos position,
                                          Long2BooleanArrayMap safetyCache, Bounds bounds) {
        float halfWidth = entity.getWidth() / 2.0F;
        for (BlockPos boxPos : BlockPos.iterate(
                Math.round(position.getX() - halfWidth), position.getY(), Math.round(position.getZ() - halfWidth),
                Math.round(position.getX() + halfWidth), Math.round(position.getY() + entity.getHeight()),
                Math.round(position.getZ() + halfWidth))) {
            if (!safetyCache.computeIfAbsent(boxPos.asLong(), ignored ->
                    destination.getWorldBorder().contains(boxPos)
                            && bounds.min < boxPos.getY()
                            && bounds.max > boxPos.getY()
                            && destination.getBlockState(boxPos).getBlock()
                            .canMobSpawnInside(destination.getBlockState(boxPos)))) {
                return false;
            }
        }
        return true;
    }

    private static void buildSafetyCage(ServerWorld destination, BlockPos position, Entity entity) {
        float halfWidth = entity.getWidth() / 2.0F;
        int minX = MathHelper.floor(position.getX() - halfWidth - 1);
        int minY = position.getY() - 1;
        int minZ = MathHelper.floor(position.getZ() - halfWidth - 1);
        int maxX = MathHelper.ceil(position.getX() + halfWidth + 1);
        int maxY = MathHelper.ceil(position.getY() + entity.getHeight() + 1);
        int maxZ = MathHelper.ceil(position.getZ() + halfWidth + 1);

        for (BlockPos cagePos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (!destination.getWorldBorder().contains(cagePos)) continue;
            BlockState state = destination.getBlockState(cagePos);
            if (state.getBlock() instanceof BlockCakeBase) continue;

            if (cagePos.getY() == minY && !state.isSolidBlock(destination, cagePos)) {
                destination.setBlockState(cagePos, Blocks.OBSIDIAN.getDefaultState());
            } else if ((cagePos.getY() == maxY || cagePos.getX() == minX || cagePos.getX() == maxX
                    || cagePos.getZ() == minZ || cagePos.getZ() == maxZ) && !state.isSolidBlock(destination, cagePos)) {
                destination.setBlockState(cagePos, Blocks.COBBLESTONE.getDefaultState());
            } else if (!state.getBlock().canMobSpawnInside(state)) {
                destination.setBlockState(cagePos, Blocks.AIR.getDefaultState());
            }
        }
    }

    public static void addDimensionPosition(ServerPlayerEntity player, RegistryKey<World> dimension, BlockPos position) {
        NbtCompound data = ((TelePastriesPlayerData) player).telepastries$getDimensionPositions();
        BlockPos saved = dimension.equals(World.END) ? ServerWorld.END_SPAWN_POS : position;
        data.putLong(key(dimension), saved.asLong());
        TelePastries.LOGGER.debug("Setting {}'s position in {} to {}", player.getName().getString(),
                dimension.getValue(), saved);
    }

    @Nullable
    public static BlockPos getDimensionPosition(Entity entity, RegistryKey<World> dimension) {
        if (!(entity instanceof TelePastriesPlayerData playerData)) return null;
        NbtCompound data = playerData.telepastries$getDimensionPositions();
        String key = key(dimension);
        if (!data.contains(key)) return null;
        BlockPos position = BlockPos.fromLong(data.getLong(key));
        TelePastries.LOGGER.debug("Found {}'s position in {}: {}", entity.getName().getString(),
                dimension.getValue(), position);
        return position;
    }

    private static String key(RegistryKey<World> dimension) {
        return dimension.getValue().toString();
    }

    private static Bounds customBounds(ServerWorld destination) {
        TeleConfig.Values config = TeleConfig.COMMON;
        if (matches(destination, config.customCakeDimension)) {
            return new Bounds(config.customCakeMinY, config.customCakeMaxY);
        }
        if (matches(destination, config.customCake2Dimension)) {
            return new Bounds(config.customCake2MinY, config.customCake2MaxY);
        }
        if (matches(destination, config.customCake3Dimension)) {
            return new Bounds(config.customCake3MinY, config.customCake3MaxY);
        }
        return new Bounds(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static boolean matches(ServerWorld destination, String configuredId) {
        Identifier id = Identifier.tryParse(configuredId);
        return id != null && destination.getRegistryKey().equals(RegistryKey.of(RegistryKeys.WORLD, id));
    }

    private static TeleportTarget finish(ServerWorld destination, BlockPos position, Entity entity) {
        RegistryKey<World> destinationKey = destination.getRegistryKey();
        if (entity instanceof ServerPlayerEntity player) {
            if (destinationKey.equals(World.OVERWORLD)
                    || FabricLoader.getInstance().isModLoaded("twilightforest")
                    && destinationKey.getValue().equals(Identifier.of("twilightforest", "twilight_forest"))
                    || FabricLoader.getInstance().isModLoaded("lostcities")
                    && destinationKey.getValue().equals(Identifier.of("lostcities", "lostcity"))) {
                player.setSpawnPoint(destinationKey, position, player.getYaw(), true, false);
            }
        }
        return new TeleportTarget(destination,
                new Vec3d(position.getX(), position.getY(), position.getZ()),
                Vec3d.ZERO, entity.getYaw(), entity.getPitch(), TeleportTarget.NO_OP);
    }

    private record Bounds(int min, int max) {
        Bounds clamp(int worldMin, int worldMax) {
            int clampedMin = Math.max(min, worldMin);
            int clampedMax = Math.min(max, worldMax);
            if (clampedMin >= clampedMax) {
                return new Bounds(worldMin, worldMax);
            }
            return new Bounds(clampedMin, clampedMax);
        }
    }
}
