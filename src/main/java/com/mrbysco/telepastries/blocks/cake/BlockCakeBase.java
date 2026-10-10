package com.mrbysco.telepastries.blocks.cake;

import com.mrbysco.telepastries.TelePastries;
import com.mrbysco.telepastries.config.TeleConfig;
import com.mrbysco.telepastries.data.TelePastriesPlayerData;
import com.mrbysco.telepastries.util.CakeTeleportHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockCakeBase extends CakeBlock {
    public BlockCakeBase(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (consumeCake() && isRefillItem(stack)) {
            if (!world.isClient) {
                if (state.get(BITES) > 0) {
                    world.setBlockState(pos, state.with(BITES, state.get(BITES) - 1), Block.NOTIFY_ALL);
                }
                stack.decrementUnlessCreative(1, player);
            }
            return ItemActionResult.SUCCESS;
        }

        RegistryKey<World> destination = getCakeWorld();
        if (TeleConfig.COMMON.resetPastry && destination != null && isResetItem(stack)) {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                removeDimensionPosition(serverPlayer, destination);
                if (stack.isOf(Items.MILK_BUCKET) && !player.isCreative()) {
                    stack.decrement(1);
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                }
            }
            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        RegistryKey<World> destination = getCakeWorld();
        if (destination == null) {
            if (!world.isClient) {
                player.sendMessage(Text.translatable("telepastries.pastry.custom.unbound").formatted(Formatting.RED), false);
            }
            return ActionResult.SUCCESS;
        }

        if (!canTeleportTo(world.getRegistryKey(), destination)) {
            if (!world.isClient) {
                String key = world.getRegistryKey().equals(destination)
                        ? "telepastries.same_dimension" : "telepastries.teleport_restricted";
                player.sendMessage(Text.translatable(key), true);
            }
            return ActionResult.SUCCESS;
        }

        ActionResult result = eatSlice(world, pos, state, player);
        if (result.isAccepted() && !world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            teleportToDimension(serverPlayer, destination);
        }
        return result;
    }

    protected ActionResult eatSlice(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!player.canConsume(TeleConfig.COMMON.ignoreHunger)) return ActionResult.PASS;

        player.incrementStat(Stats.EAT_CAKE_SLICE);
        player.getHungerManager().add(2, 0.1F);
        world.emitGameEvent(player, GameEvent.EAT, pos);
        if (consumeCake() && !player.isCreative()) {
            int bites = state.get(BITES);
            if (bites < MAX_BITES) {
                world.setBlockState(pos, state.with(BITES, bites + 1), Block.NOTIFY_ALL);
            } else {
                world.removeBlock(pos, false);
                world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }
        }
        return ActionResult.SUCCESS;
    }

    public boolean canTeleportTo(RegistryKey<World> current, RegistryKey<World> destination) {
        if (!TeleConfig.COMMON.disableHopping) return !current.equals(destination);
        return current.equals(World.OVERWORLD)
                ? !destination.equals(World.OVERWORLD)
                : destination.equals(World.OVERWORLD);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (TeleConfig.COMMON.disableHopping && world instanceof World actualWorld) {
            RegistryKey<World> destination = getCakeWorld();
            if (destination == null) return false;
            RegistryKey<World> current = actualWorld.getRegistryKey();
            return current.equals(World.OVERWORLD)
                    ? !destination.equals(World.OVERWORLD)
                    : destination.equals(World.OVERWORLD);
        }
        return super.canPlaceAt(state, world, pos);
    }

    protected void teleportToDimension(ServerPlayerEntity player, RegistryKey<World> destination) {
        if (!player.isAlive() || player.hasPassengers() || player.hasVehicle()) return;

        ServerWorld destinationWorld = player.getServer().getWorld(destination);
        if (destinationWorld == null) {
            player.sendMessage(Text.translatable("telepastries.pastry.custom.invalid", destination.getValue())
                    .formatted(Formatting.RED), false);
            TelePastries.LOGGER.error("Destination of cake invalid: {} isn't known", destination.getValue());
            return;
        }

        CakeTeleportHelper.addDimensionPosition(player, player.getWorld().getRegistryKey(), player.getBlockPos());
        player.teleportTo(CakeTeleportHelper.getCakeTeleportTarget(destinationWorld, player));
    }

    protected boolean matchesConfiguredItem(ItemStack stack, List<String> items) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (items.contains(id.toString())) return true;
        // Upstream retains the pre-flattening minecraft:bed default. Treat it as the intended bed family.
        return items.contains("minecraft:bed")
                && id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
                && id.getPath().endsWith("_bed");
    }

    private boolean isResetItem(ItemStack stack) {
        return matchesConfiguredItem(stack, TeleConfig.COMMON.resetItems);
    }

    protected void removeDimensionPosition(ServerPlayerEntity player, RegistryKey<World> dimension) {
        TelePastriesPlayerData data = (TelePastriesPlayerData) player;
        String key = dimension.getValue().toString();
        if (data.telepastries$getDimensionPositions().contains(key)) {
            data.telepastries$getDimensionPositions().remove(key);
            player.sendMessage(Text.translatable("telepastries.pastry.reset.complete", dimension.getValue()), false);
        } else {
            player.sendMessage(Text.translatable("telepastries.pastry.reset.failed", dimension.getValue()), false);
        }
    }

    public boolean isRefillItem(ItemStack stack) {
        return false;
    }

    @Nullable
    public RegistryKey<World> getCakeWorld() {
        return World.OVERWORLD;
    }

    public boolean consumeCake() {
        return true;
    }
}
