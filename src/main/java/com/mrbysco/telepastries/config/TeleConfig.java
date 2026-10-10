package com.mrbysco.telepastries.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mrbysco.telepastries.TelePastries;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lightweight JSON replacement for the upstream NeoForge common config. */
public final class TeleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("telepastries.json");

    public static Values COMMON = new Values();

    private TeleConfig() {
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                Values loaded = GSON.fromJson(reader, Values.class);
                if (loaded != null) COMMON = loaded;
            } catch (IOException | JsonParseException exception) {
                TelePastries.LOGGER.error("Could not read {}; using defaults", PATH, exception);
                COMMON = new Values();
            }
        }

        COMMON.sanitize();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(COMMON, writer);
            }
        } catch (IOException exception) {
            TelePastries.LOGGER.error("Could not write {}", PATH, exception);
        }
    }

    public static final class Values {
        public boolean explosionImmune = false;
        public boolean ignoreHunger = false;
        public boolean disableHopping = true;
        public boolean resetPastry = false;
        public List<String> resetItems = list("minecraft:milk_bucket");

        public boolean consumeNetherCake = true;
        public List<String> netherCakeRefillItems = list("minecraft:obsidian");
        public boolean consumeEndCake = true;
        public List<String> endCakeRefillItems = list("minecraft:ender_eye");
        public boolean consumeOverworldCake = true;
        public List<String> overworldCakeRefillItems = list(
                "minecraft:oak_sapling", "minecraft:spruce_sapling", "minecraft:birch_sapling",
                "minecraft:jungle_sapling", "minecraft:acacia_sapling", "minecraft:dark_oak_sapling");

        public boolean consumeTwilightCake = true;
        public List<String> twilightCakeRefillItems = list("minecraft:diamond");
        public boolean consumeLostCitiesCake = true;
        public List<String> lostCitiesCakeRefillItem = list("minecraft:bed");

        public boolean consumeCustomCake = true;
        public String customCakeName = "Custom";
        public String customCakeDimension = "minecraft:overworld";
        public List<String> customCakeRefillItem = list("minecraft:cobblestone");
        public int customCakeMinY = 2;
        public int customCakeMaxY = 254;

        public boolean consumeCustomCake2 = true;
        public String customCake2Name = "Another Custom";
        public String customCake2Dimension = "minecraft:overworld";
        public List<String> customCake2RefillItem = list("minecraft:cobblestone");
        public int customCake2MinY = 2;
        public int customCake2MaxY = 254;

        public boolean consumeCustomCake3 = true;
        public String customCake3Name = "Yet Another Custom";
        public String customCake3Dimension = "minecraft:overworld";
        public List<String> customCake3RefillItem = list("minecraft:cobblestone");
        public int customCake3MinY = 2;
        public int customCake3MaxY = 254;

        private void sanitize() {
            resetItems = safe(resetItems);
            netherCakeRefillItems = safe(netherCakeRefillItems);
            endCakeRefillItems = safe(endCakeRefillItems);
            overworldCakeRefillItems = safe(overworldCakeRefillItems);
            twilightCakeRefillItems = safe(twilightCakeRefillItems);
            lostCitiesCakeRefillItem = safe(lostCitiesCakeRefillItem);
            customCakeRefillItem = safe(customCakeRefillItem);
            customCake2RefillItem = safe(customCake2RefillItem);
            customCake3RefillItem = safe(customCake3RefillItem);
            customCakeName = safe(customCakeName, "Custom");
            customCake2Name = safe(customCake2Name, "Another Custom");
            customCake3Name = safe(customCake3Name, "Yet Another Custom");
            customCakeDimension = safe(customCakeDimension, "");
            customCake2Dimension = safe(customCake2Dimension, "");
            customCake3Dimension = safe(customCake3Dimension, "");
            customCakeMinY = clamp(customCakeMinY);
            customCakeMaxY = clamp(customCakeMaxY);
            customCake2MinY = clamp(customCake2MinY);
            customCake2MaxY = clamp(customCake2MaxY);
            customCake3MinY = clamp(customCake3MinY);
            customCake3MaxY = clamp(customCake3MaxY);
        }

        private static int clamp(int value) {
            return Math.max(1, Math.min(256, value));
        }

        private static String safe(String value, String fallback) {
            return value == null ? fallback : value;
        }

        private static List<String> safe(List<String> values) {
            if (values == null) return new ArrayList<>();
            values.removeIf(value -> value == null || value.isBlank());
            return values;
        }

        private static List<String> list(String... values) {
            return new ArrayList<>(List.of(values));
        }
    }
}
