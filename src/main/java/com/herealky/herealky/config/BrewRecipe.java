package com.herealky.herealky.config;

import org.bukkit.Material;
import org.bukkit.potion.PotionType;

public enum BrewRecipe {
    SWIFTNESS_NORMAL("Potion of Swiftness", PotionType.SWIFTNESS, Material.NETHER_WART, Material.SUGAR, null, 2, 10.0, 25.0, 0.0),
    SWIFTNESS_EXTENDED("Extended Potion of Swiftness", PotionType.LONG_SWIFTNESS, Material.NETHER_WART, Material.SUGAR, Material.REDSTONE, 3, 10.0, 20.0, 30.0),
    SWIFTNESS_ENHANCED("Potion of Swiftness II", PotionType.STRONG_SWIFTNESS, Material.NETHER_WART, Material.SUGAR, Material.GLOWSTONE, 3, 10.0, 20.0, 40.0),
    
    HEALING_NORMAL("Potion of Healing", PotionType.HEALING, Material.NETHER_WART, Material.GLISTERING_MELON_SLICE, null, 2, 10.0, 30.0, 0.0),
    HEALING_ENHANCED("Potion of Healing II", PotionType.STRONG_HEALING, Material.NETHER_WART, Material.GLISTERING_MELON_SLICE, Material.GLOWSTONE, 3, 10.0, 20.0, 50.0),
    
    STRENGTH_NORMAL("Potion of Strength", PotionType.STRENGTH, Material.NETHER_WART, Material.BLAZE_POWDER, null, 2, 10.0, 25.0, 0.0),
    STRENGTH_EXTENDED("Extended Potion of Strength", PotionType.LONG_STRENGTH, Material.NETHER_WART, Material.BLAZE_POWDER, Material.REDSTONE, 3, 10.0, 20.0, 30.0),
    STRENGTH_ENHANCED("Potion of Strength II", PotionType.STRONG_STRENGTH, Material.NETHER_WART, Material.BLAZE_POWDER, Material.GLOWSTONE, 3, 10.0, 20.0, 40.0),
    
    REGENERATION_NORMAL("Potion of Regeneration", PotionType.REGENERATION, Material.NETHER_WART, Material.GHAST_TEAR, null, 2, 10.0, 40.0, 0.0),
    REGENERATION_EXTENDED("Extended Potion of Regeneration", PotionType.LONG_REGENERATION, Material.NETHER_WART, Material.GHAST_TEAR, Material.REDSTONE, 3, 10.0, 30.0, 40.0),
    REGENERATION_ENHANCED("Potion of Regeneration II", PotionType.STRONG_REGENERATION, Material.NETHER_WART, Material.GHAST_TEAR, Material.GLOWSTONE, 3, 10.0, 30.0, 50.0),
    
    SLOW_FALLING_NORMAL("Potion of Slow Falling", PotionType.SLOW_FALLING, Material.NETHER_WART, Material.PHANTOM_MEMBRANE, null, 2, 10.0, 30.0, 0.0),
    SLOW_FALLING_EXTENDED("Extended Potion of Slow Falling", PotionType.LONG_SLOW_FALLING, Material.NETHER_WART, Material.PHANTOM_MEMBRANE, Material.REDSTONE, 3, 10.0, 25.0, 35.0),
    
    NIGHT_VISION_NORMAL("Potion of Night Vision", PotionType.NIGHT_VISION, Material.NETHER_WART, Material.GOLDEN_CARROT, null, 2, 10.0, 25.0, 0.0),
    NIGHT_VISION_EXTENDED("Extended Potion of Night Vision", PotionType.LONG_NIGHT_VISION, Material.NETHER_WART, Material.GOLDEN_CARROT, Material.REDSTONE, 3, 10.0, 20.0, 30.0),
    
    WATER_BREATHING_NORMAL("Potion of Water Breathing", PotionType.WATER_BREATHING, Material.NETHER_WART, Material.PUFFERFISH, null, 2, 10.0, 25.0, 0.0),
    WATER_BREATHING_EXTENDED("Extended Potion of Water Breathing", PotionType.LONG_WATER_BREATHING, Material.NETHER_WART, Material.PUFFERFISH, Material.REDSTONE, 3, 10.0, 20.0, 30.0),
    
    FIRE_RESISTANCE_NORMAL("Potion of Fire Resistance", PotionType.FIRE_RESISTANCE, Material.NETHER_WART, Material.MAGMA_CREAM, null, 2, 10.0, 25.0, 0.0),
    FIRE_RESISTANCE_EXTENDED("Extended Potion of Fire Resistance", PotionType.LONG_FIRE_RESISTANCE, Material.NETHER_WART, Material.MAGMA_CREAM, Material.REDSTONE, 3, 10.0, 20.0, 30.0),
    
    LEAPING_NORMAL("Potion of Leaping", PotionType.LEAPING, Material.NETHER_WART, Material.RABBIT_FOOT, null, 2, 10.0, 25.0, 0.0),
    LEAPING_EXTENDED("Extended Potion of Leaping", PotionType.LONG_LEAPING, Material.NETHER_WART, Material.RABBIT_FOOT, Material.REDSTONE, 3, 10.0, 20.0, 30.0),
    LEAPING_ENHANCED("Potion of Leaping II", PotionType.STRONG_LEAPING, Material.NETHER_WART, Material.RABBIT_FOOT, Material.GLOWSTONE, 3, 10.0, 20.0, 40.0),

    // Custom Recipes (Unlocked at 10 brews)
    MAGNETISM_NORMAL("Potion of Magnetism", PotionType.AWKWARD, Material.NETHER_WART, Material.LODESTONE, null, 2, 20.0, 50.0, 0.0),
    MAGNETISM_EXTENDED("Extended Potion of Magnetism", PotionType.AWKWARD, Material.NETHER_WART, Material.LODESTONE, Material.REDSTONE, 3, 20.0, 40.0, 60.0),
    MAGNETISM_ONE_HOUR("1-Hour Potion of Magnetism", PotionType.AWKWARD, Material.NETHER_WART, Material.LODESTONE, Material.NETHER_WART_BLOCK, 3, 20.0, 40.0, 100.0),

    TRUE_SIGHT_NORMAL("Potion of True Sight", PotionType.AWKWARD, Material.NETHER_WART, Material.AMETHYST_SHARD, null, 2, 20.0, 50.0, 0.0),
    TRUE_SIGHT_EXTENDED("Extended Potion of True Sight", PotionType.AWKWARD, Material.NETHER_WART, Material.AMETHYST_SHARD, Material.REDSTONE, 3, 20.0, 40.0, 60.0),
    TRUE_SIGHT_ONE_HOUR("1-Hour Potion of True Sight", PotionType.AWKWARD, Material.NETHER_WART, Material.AMETHYST_SHARD, Material.NETHER_WART_BLOCK, 3, 20.0, 40.0, 100.0),

    FEATHERWEIGHT_NORMAL("Potion of Featherweight", PotionType.AWKWARD, Material.NETHER_WART, Material.PHANTOM_MEMBRANE, null, 2, 20.0, 50.0, 0.0),
    FEATHERWEIGHT_EXTENDED("Extended Potion of Featherweight", PotionType.AWKWARD, Material.NETHER_WART, Material.PHANTOM_MEMBRANE, Material.REDSTONE, 3, 20.0, 40.0, 60.0),
    FEATHERWEIGHT_ONE_HOUR("1-Hour Potion of Featherweight", PotionType.AWKWARD, Material.NETHER_WART, Material.PHANTOM_MEMBRANE, Material.NETHER_WART_BLOCK, 3, 20.0, 40.0, 100.0),

    OBSIDIAN_SKIN_NORMAL("Potion of Obsidian Skin", PotionType.AWKWARD, Material.NETHER_WART, Material.CRYING_OBSIDIAN, null, 2, 20.0, 50.0, 0.0),
    OBSIDIAN_SKIN_EXTENDED("Extended Potion of Obsidian Skin", PotionType.AWKWARD, Material.NETHER_WART, Material.CRYING_OBSIDIAN, Material.REDSTONE, 3, 20.0, 40.0, 60.0),
    OBSIDIAN_SKIN_ONE_HOUR("1-Hour Potion of Obsidian Skin", PotionType.AWKWARD, Material.NETHER_WART, Material.CRYING_OBSIDIAN, Material.NETHER_WART_BLOCK, 3, 20.0, 40.0, 100.0),

    // Custom Recipe (Unlocked at 1000 brews)
    RANDOM_EFFECT_NORMAL("Potion of Chaos", PotionType.AWKWARD, Material.NETHER_WART, Material.CHORUS_FRUIT, null, 2, 50.0, 100.0, 0.0),
    RANDOM_EFFECT_EXTENDED("Extended Potion of Chaos", PotionType.AWKWARD, Material.NETHER_WART, Material.CHORUS_FRUIT, Material.REDSTONE, 3, 50.0, 80.0, 120.0),
    RANDOM_EFFECT_ONE_HOUR("1-Hour Potion of Chaos", PotionType.AWKWARD, Material.NETHER_WART, Material.CHORUS_FRUIT, Material.NETHER_WART_BLOCK, 3, 50.0, 80.0, 200.0);

    private final String displayName;
    private final PotionType basePotionType;
    private final Material stage1Ingredient;
    private final Material stage2Ingredient;
    private final Material stage3Ingredient;
    private final int stepsCount;
    private final double stage1Xp;
    private final double stage2Xp;
    private final double stage3Xp;

    BrewRecipe(String displayName, PotionType basePotionType, Material stage1Ingredient, Material stage2Ingredient, Material stage3Ingredient,
               int stepsCount, double stage1Xp, double stage2Xp, double stage3Xp) {
        this.displayName = displayName;
        this.basePotionType = basePotionType;
        this.stage1Ingredient = stage1Ingredient;
        this.stage2Ingredient = stage2Ingredient;
        this.stage3Ingredient = stage3Ingredient;
        this.stepsCount = stepsCount;
        this.stage1Xp = stage1Xp;
        this.stage2Xp = stage2Xp;
        this.stage3Xp = stage3Xp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PotionType getBasePotionType() {
        return basePotionType;
    }

    public Material getStage1Ingredient() {
        return stage1Ingredient;
    }

    public Material getStage2Ingredient() {
        return stage2Ingredient;
    }

    public Material getStage3Ingredient() {
        return stage3Ingredient;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public double getStageXp(int stage) {
        if (stage == 1) return stage1Xp;
        if (stage == 2) return stage2Xp;
        if (stage == 3) return stage3Xp;
        return 0.0;
    }

    public Material getStageIngredient(int stage) {
        if (stage == 1) return stage1Ingredient;
        if (stage == 2) return stage2Ingredient;
        if (stage == 3) return stage3Ingredient;
        return null;
    }
}
