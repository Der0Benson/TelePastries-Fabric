package com.mrbysco.telepastries.blocks.cake;

import com.mrbysco.telepastries.config.TeleConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class BlockEndCake extends BlockCakeBase {
    public BlockEndCake(Settings settings) { super(settings); }

    @Override public boolean isRefillItem(ItemStack stack) {
        return matchesConfiguredItem(stack, TeleConfig.COMMON.endCakeRefillItems);
    }

    @Override public RegistryKey<World> getCakeWorld() { return World.END; }

    @Override public boolean consumeCake() { return TeleConfig.COMMON.consumeEndCake; }
}
