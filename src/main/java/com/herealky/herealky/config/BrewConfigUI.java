package com.herealky.herealky.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrewConfigUI {

    private final BrewConfigManager configManager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public BrewConfigUI(BrewConfigManager configManager) {
        this.configManager = configManager;
    }

    public void openMainMenu(Player player) {
        playerPages.put(player.getUniqueId(), 1);
        openPage(player, 1);
    }

    public void openPage(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        String title = "HereAlky Recipes - Page " + page;
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(title));

        // Fill background borders & fillers
        ItemStack blackGlass = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ", new ArrayList<>());
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, blackGlass);
        }

        // Add page controls
        if (page == 1) {
            ItemStack nextPage = createGuiItem(Material.ARROW, "→ Next Page (Utility)", List.of("Click to view utility potions"));
            inv.setItem(53, nextPage);
        } else {
            ItemStack prevPage = createGuiItem(Material.ARROW, "← Previous Page (Combat)", List.of("Click to view combat potions"));
            inv.setItem(45, prevPage);
        }

        // Fetch recipes for the current page
        List<BrewRecipe> recipes = getRecipesForPage(page);

        // Map layout:
        // Potions: Row 1 (slots 10-16), Row 3 (slots 28-34)
        // Backgrounds: Row 2 (slots 19-25), Row 4 (slots 37-43)
        int index = 0;
        for (BrewRecipe recipe : recipes) {
            int potionSlot;
            int bgSlot;

            if (index < 7) {
                potionSlot = 10 + index;
                bgSlot = 19 + index;
            } else {
                potionSlot = 28 + (index - 7);
                bgSlot = 37 + (index - 7);
            }

            // Potion Item
            ItemStack potionItem = createPotionItem(recipe);
            inv.setItem(potionSlot, potionItem);

            // Background Indicator (checks player's inventory)
            ItemStack bgPane = createBackgroundPane(player, recipe);
            inv.setItem(bgSlot, bgPane);

            index++;
        }

        player.openInventory(inv);
    }

    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }

    private List<BrewRecipe> getRecipesForPage(int page) {
        List<BrewRecipe> combat = new ArrayList<>();
        List<BrewRecipe> utility = new ArrayList<>();

        for (BrewRecipe recipe : BrewRecipe.values()) {
            if (recipe.name().contains("SWIFTNESS") || recipe.name().contains("HEALING") ||
                recipe.name().contains("STRENGTH") || recipe.name().contains("REGENERATION")) {
                combat.add(recipe);
            } else {
                utility.add(recipe);
            }
        }

        return page == 1 ? combat : utility;
    }

    public BrewRecipe getRecipeFromSlot(int page, int slot) {
        List<BrewRecipe> recipes = getRecipesForPage(page);
        int index = -1;

        if (slot >= 10 && slot <= 16) {
            index = slot - 10;
        } else if (slot >= 28 && slot <= 34) {
            index = slot - 28 + 7;
        }

        if (index >= 0 && index < recipes.size()) {
            return recipes.get(index);
        }
        return null;
    }

    private ItemStack createPotionItem(BrewRecipe recipe) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(recipe.getBasePotionType());
            meta.displayName(Component.text(recipe.getDisplayName())
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("-----------------------").color(NamedTextColor.GRAY));
            lore.add(Component.text("✦ Brewing Steps (" + recipe.getStepsCount() + "):").color(NamedTextColor.YELLOW));
            lore.add(Component.text("  1. Water Bottle + " + formatMatName(recipe.getStage1Ingredient())).color(NamedTextColor.GRAY));
            lore.add(Component.text("  2. Awkward Potion + " + formatMatName(recipe.getStage2Ingredient())).color(NamedTextColor.GRAY));
            if (recipe.getStepsCount() == 3) {
                lore.add(Component.text("  3. Effect Potion + " + formatMatName(recipe.getStage3Ingredient())).color(NamedTextColor.GRAY));
            }
            lore.add(Component.text("-----------------------").color(NamedTextColor.GRAY));
            lore.add(Component.text("Click to configure this recipe!").color(NamedTextColor.GREEN));

            meta.lore(lore);
            potion.setItemMeta(meta);
        }
        return potion;
    }

    private ItemStack createBackgroundPane(Player player, BrewRecipe recipe) {
        // Scan inventory for ingredients
        boolean hasS1 = player.getInventory().contains(recipe.getStage1Ingredient());
        boolean hasS2 = player.getInventory().contains(recipe.getStage2Ingredient());
        boolean hasS3 = recipe.getStepsCount() < 3 || player.getInventory().contains(recipe.getStage3Ingredient());
        boolean allAvailable = hasS1 && hasS2 && hasS3;

        Material paneMat = allAvailable ? Material.GRAY_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String title = allAvailable ? "✔ Ingredients Available" : "❌ Ingredients Missing";
        NamedTextColor titleColor = allAvailable ? NamedTextColor.GRAY : NamedTextColor.RED;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Checklist for " + recipe.getDisplayName() + ":").color(NamedTextColor.YELLOW));
        
        lore.add(Component.text(
            (hasS1 ? " ✔ " : " ❌ ") + "Stage 1: " + formatMatName(recipe.getStage1Ingredient())
        ).color(hasS1 ? NamedTextColor.GREEN : NamedTextColor.RED));

        lore.add(Component.text(
            (hasS2 ? " ✔ " : " ❌ ") + "Stage 2: " + formatMatName(recipe.getStage2Ingredient())
        ).color(hasS2 ? NamedTextColor.GREEN : NamedTextColor.RED));

        if (recipe.getStepsCount() == 3) {
            lore.add(Component.text(
                (hasS3 ? " ✔ " : " ❌ ") + "Stage 3: " + formatMatName(recipe.getStage3Ingredient())
            ).color(hasS3 ? NamedTextColor.GREEN : NamedTextColor.RED));
        }

        ItemStack pane = new ItemStack(paneMat);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(title).color(titleColor).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> loreStrs) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            for (String str : loreStrs) {
                lore.add(Component.text(str).color(NamedTextColor.GRAY));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatMatName(Material mat) {
        if (mat == null) return "None";
        String name = mat.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
