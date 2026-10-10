package com.mrbysco.telepastries.init;

import com.mrbysco.telepastries.Reference;
import com.mrbysco.telepastries.blocks.cake.BlockEndCake;
import com.mrbysco.telepastries.blocks.cake.BlockNetherCake;
import com.mrbysco.telepastries.blocks.cake.BlockOverworldCake;
import com.mrbysco.telepastries.blocks.cake.compat.BlockCustomCake;
import com.mrbysco.telepastries.blocks.cake.compat.BlockCustomCake2;
import com.mrbysco.telepastries.blocks.cake.compat.BlockCustomCake3;
import com.mrbysco.telepastries.blocks.cake.compat.BlockLostCityCake;
import com.mrbysco.telepastries.blocks.cake.compat.BlockTwilightCake;
import com.mrbysco.telepastries.config.TeleConfig;
import com.mrbysco.telepastries.item.CakeBlockItem;
import com.mrbysco.telepastries.item.CustomCake2BlockItem;
import com.mrbysco.telepastries.item.CustomCake3BlockItem;
import com.mrbysco.telepastries.item.CustomCakeBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Function;

public final class TeleRegistry {
    public static final BlockNetherCake NETHER_CAKE = registerBlock("nether_cake", BlockNetherCake::new);
    public static final BlockEndCake END_CAKE = registerBlock("end_cake", BlockEndCake::new);
    public static final BlockOverworldCake OVERWORLD_CAKE = registerBlock("overworld_cake", BlockOverworldCake::new);
    public static final BlockTwilightCake TWILIGHT_CAKE = registerBlock("twilight_cake", BlockTwilightCake::new);
    public static final BlockLostCityCake LOST_CITY_CAKE = registerBlock("lost_city_cake", BlockLostCityCake::new);
    public static final BlockCustomCake CUSTOM_CAKE = registerBlock("custom_cake", BlockCustomCake::new);
    public static final BlockCustomCake2 CUSTOM_CAKE2 = registerBlock("custom_cake2", BlockCustomCake2::new);
    public static final BlockCustomCake3 CUSTOM_CAKE3 = registerBlock("custom_cake3", BlockCustomCake3::new);

    public static final CakeBlockItem NETHER_CAKE_ITEM = registerItem("nether_cake", new CakeBlockItem(NETHER_CAKE, new Item.Settings()));
    public static final CakeBlockItem END_CAKE_ITEM = registerItem("end_cake", new CakeBlockItem(END_CAKE, new Item.Settings()));
    public static final CakeBlockItem OVERWORLD_CAKE_ITEM = registerItem("overworld_cake", new CakeBlockItem(OVERWORLD_CAKE, new Item.Settings()));
    public static final CakeBlockItem TWILIGHT_CAKE_ITEM = registerItem("twilight_cake", new CakeBlockItem(TWILIGHT_CAKE, new Item.Settings()));
    public static final CakeBlockItem LOST_CITY_CAKE_ITEM = registerItem("lost_city_cake", new CakeBlockItem(LOST_CITY_CAKE, new Item.Settings()));
    public static final CakeBlockItem CUSTOM_CAKE_ITEM = registerItem("custom_cake", new CustomCakeBlockItem(CUSTOM_CAKE, new Item.Settings()));
    public static final CakeBlockItem CUSTOM_CAKE2_ITEM = registerItem("custom_cake2", new CustomCake2BlockItem(CUSTOM_CAKE2, new Item.Settings()));
    public static final CakeBlockItem CUSTOM_CAKE3_ITEM = registerItem("custom_cake3", new CustomCake3BlockItem(CUSTOM_CAKE3, new Item.Settings()));

    private static final List<Item> ITEMS = List.of(
            NETHER_CAKE_ITEM, END_CAKE_ITEM, OVERWORLD_CAKE_ITEM, TWILIGHT_CAKE_ITEM,
            LOST_CITY_CAKE_ITEM, CUSTOM_CAKE_ITEM, CUSTOM_CAKE2_ITEM, CUSTOM_CAKE3_ITEM);

    public static final ItemGroup PASTRIES_TAB = Registry.register(Registries.ITEM_GROUP, id("tab"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(OVERWORLD_CAKE_ITEM))
                    .displayName(Text.translatable("itemGroup.tele_tab"))
                    .entries((context, entries) -> ITEMS.forEach(entries::add))
                    .build());

    private TeleRegistry() {
    }

    public static void initialize() {
        // Loading this class performs the vanilla registry registrations.
    }

    private static AbstractBlock.Settings cakeSettings() {
        float blastResistance = TeleConfig.COMMON.explosionImmune ? 3_600_000.0F : 0.5F;
        return AbstractBlock.Settings.create().solid().strength(0.5F, blastResistance)
                .sounds(BlockSoundGroup.WOOL).pistonBehavior(PistonBehavior.DESTROY);
    }

    private static <T extends Block> T registerBlock(String name, Function<AbstractBlock.Settings, T> factory) {
        return Registry.register(Registries.BLOCK, id(name), factory.apply(cakeSettings()));
    }

    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    private static Identifier id(String path) {
        return Identifier.of(Reference.MOD_ID, path);
    }
}
