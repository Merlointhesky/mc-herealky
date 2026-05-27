package com.herealky.herealky.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BrewConfigListener implements Listener {

    private final BrewConfigUI configUI;
    private final BrewConfigManager configManager;

    public BrewConfigListener(BrewConfigUI configUI, BrewConfigManager configManager) {
        this.configUI = configUI;
        this.configManager = configManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.startsWith("HereAlky Recipes - Page")) return;

        event.setCancelled(true); // Prevent item stealing

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int page = configUI.getPlayerPage(player);
        int slot = event.getRawSlot();
        UUID uuid = player.getUniqueId();

        // Check page navigation controls
        if (slot == 53 && page == 1) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            configUI.openPage(player, 2);
            return;
        }

        if (slot == 45 && page == 2) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            configUI.openPage(player, 1);
            return;
        }

        // Check if recipe is clicked
        BrewRecipe recipe = configUI.getRecipeFromSlot(page, slot);
        if (recipe != null) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);

            PlayerBrewConfig config = configManager.getPlayerConfig(uuid);
            config.setSelectedRecipe(recipe);
            configManager.saveConfiguration(uuid);

            player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("       Recipe Selected!       ").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
            player.sendMessage(Component.text(" ★ Selected: " + recipe.getDisplayName()).color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text(" ✦ Ingredients Required:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("   - Stage 1: " + formatMatName(recipe.getStage1Ingredient())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("   - Stage 2: " + formatMatName(recipe.getStage2Ingredient())).color(NamedTextColor.GRAY));
            if (recipe.getStepsCount() == 3) {
                player.sendMessage(Component.text("   - Stage 3: " + formatMatName(recipe.getStage3Ingredient())).color(NamedTextColor.GRAY));
            }
            player.sendMessage(Component.text(" ✦ Setup Instructions:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("   1. Put all ingredients into the central Ingredient Box.").color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("   2. Run '/ha select' to toggle selection mode.").color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("   3. Shift-Right-Click with an Empty Glass Bottle to select Point A and Point B surrounding your brewing stands.").color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("   4. Once mapping is complete, type '/ha start' to begin batch processing!").color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        }
    }

    private String formatMatName(org.bukkit.Material mat) {
        if (mat == null) return "None";
        String name = mat.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
