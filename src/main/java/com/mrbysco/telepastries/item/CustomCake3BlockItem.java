package com.mrbysco.telepastries.item;

import com.mrbysco.telepastries.config.TeleConfig;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CustomCake3BlockItem extends CakeBlockItem {
    public CustomCake3BlockItem(Block block, Settings settings) { super(block, settings); }

    @Override public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey(stack), TeleConfig.COMMON.customCake3Name);
    }
}
