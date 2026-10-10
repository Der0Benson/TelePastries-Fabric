package com.mrbysco.telepastries.blocks.cake;

import com.mrbysco.telepastries.config.TeleConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class BlockNetherCake extends BlockCakeBase {
    public BlockNetherCake(Settings settings) { super(settings); }

    @Override public boolean isRefillItem(ItemStack stack) {
        return matchesConfiguredItem(stack, TeleConfig.COMMON.netherCakeRefillItems);
    }

    @Override public RegistryKey<World> getCakeWorld() { return World.NETHER; }

    @Override public boolean consumeCake() { return TeleConfig.COMMON.consumeNetherCake; }
}
