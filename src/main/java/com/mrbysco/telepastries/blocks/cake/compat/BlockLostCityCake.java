package com.mrbysco.telepastries.blocks.cake.compat;

import com.mrbysco.telepastries.blocks.cake.BlockCakeBase;
import com.mrbysco.telepastries.config.TeleConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockLostCityCake extends BlockCakeBase {
    private static final RegistryKey<World> LOST_CITY = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of("lostcities", "lostcity"));

    public BlockLostCityCake(Settings settings) { super(settings); }

    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                           PlayerEntity player, BlockHitResult hit) {
        if (!FabricLoader.getInstance().isModLoaded("lostcities")) {
            if (!world.isClient) player.sendMessage(Text.translatable(
                    "telepastries.pastry.support.disabled", "lostcities").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override public boolean isRefillItem(ItemStack stack) {
        return matchesConfiguredItem(stack, TeleConfig.COMMON.lostCitiesCakeRefillItem);
    }

    @Override public RegistryKey<World> getCakeWorld() { return LOST_CITY; }

    @Override public boolean consumeCake() { return TeleConfig.COMMON.consumeLostCitiesCake; }
}
