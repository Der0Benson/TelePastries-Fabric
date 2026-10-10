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

public class BlockTwilightCake extends BlockCakeBase {
    private static final RegistryKey<World> TWILIGHT = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of("twilightforest", "twilight_forest"));

    public BlockTwilightCake(Settings settings) { super(settings); }

    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                           PlayerEntity player, BlockHitResult hit) {
        if (!FabricLoader.getInstance().isModLoaded("twilightforest")) {
            if (!world.isClient) player.sendMessage(Text.translatable(
                    "telepastries.pastry.support.disabled", "twilightforest").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override public boolean isRefillItem(ItemStack stack) {
        return matchesConfiguredItem(stack, TeleConfig.COMMON.twilightCakeRefillItems);
    }

    @Override public RegistryKey<World> getCakeWorld() { return TWILIGHT; }

    @Override public boolean consumeCake() { return TeleConfig.COMMON.consumeTwilightCake; }
}
