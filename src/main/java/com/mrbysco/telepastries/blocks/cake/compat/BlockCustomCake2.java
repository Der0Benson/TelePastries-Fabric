package com.mrbysco.telepastries.blocks.cake.compat;

import com.mrbysco.telepastries.blocks.cake.BlockCakeBase;
import com.mrbysco.telepastries.config.TeleConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockCustomCake2 extends BlockCakeBase {
    public BlockCustomCake2(Settings settings) { super(settings); }

    @Override public MutableText getName() {
        return Text.translatable(getTranslationKey(), TeleConfig.COMMON.customCake2Name);
    }

    @Override public boolean isRefillItem(ItemStack stack) {
        return matchesConfiguredItem(stack, TeleConfig.COMMON.customCake2RefillItem);
    }

    @Nullable @Override public RegistryKey<World> getCakeWorld() {
        Identifier id = Identifier.tryParse(TeleConfig.COMMON.customCake2Dimension);
        return id == null || TeleConfig.COMMON.customCake2Dimension.isBlank() ? null : RegistryKey.of(RegistryKeys.WORLD, id);
    }

    @Override public boolean consumeCake() { return TeleConfig.COMMON.consumeCustomCake2; }
}
