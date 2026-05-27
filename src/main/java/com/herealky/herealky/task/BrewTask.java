package com.herealky.herealky.task;

import com.herealky.herealky.HereAlkyPlugin;
import com.herealky.herealky.auraskills.AuraSkillsHelper;
import com.herealky.herealky.config.BrewRecipe;
import com.herealky.herealky.config.PlayerBrewConfig;
import com.herealky.herealky.setup.SetupConfiguration;
import com.herealky.herealky.setup.SetupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BrewTask extends BukkitRunnable {

    private final HereAlkyPlugin plugin;
    private final Player player;
    private final PlayerBrewConfig brewConfig;
    private final SetupConfiguration setupConfig;
    private final SetupManager setupManager;
    private final AuraSkillsHelper auraSkillsHelper;

    // Stats tracking
    private int potionsCompletedCount = 0;
    private long startTime;

    public BrewTask(HereAlkyPlugin plugin, Player player, PlayerBrewConfig brewConfig, SetupConfiguration setupConfig) {
        this.plugin = plugin;
        this.player = player;
        this.brewConfig = brewConfig;
        this.setupConfig = setupConfig;
        this.setupManager = plugin.getSetupManager();
        this.auraSkillsHelper = plugin.getAuraSkillsHelper();
        this.startTime = System.currentTimeMillis();
    }

    public HereAlkyPlugin getPlugin() {
        return plugin;
    }

    public void incrementPotionsCompleted(int amount) {
        this.potionsCompletedCount += amount;
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        plugin.getBrewTaskManager().removeActiveTask(player.getUniqueId());
        super.cancel();
    }

    public void stopTask(String reason, NamedTextColor color) {
        sendActivitySummary(reason, color);
        cancel();
    }

    public void sendActivitySummary(String reason, NamedTextColor color) {
        long durationSec = (System.currentTimeMillis() - startTime) / 1000;
        player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 0.8f);
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("       Brewing Batch Stopped       ").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text(" ★ Reason: " + reason).color(color));
        player.sendMessage(Component.text(" ★ Potion Recipe: " + (brewConfig.getSelectedRecipe() != null ? brewConfig.getSelectedRecipe().getDisplayName() : "None")).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" ✦ Batch Stats:").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("   - Potion Bottles Completed: " + potionsCompletedCount).color(NamedTextColor.GREEN));
        player.sendMessage(Component.text("   - Active Runtime: " + durationSec + " seconds").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        // Proximity Range Check (within 64 blocks of empty bottle chest)
        Location emptyBottleChestLoc = setupConfig.getEmptyBottleBox();
        if (emptyBottleChestLoc == null || !emptyBottleChestLoc.getWorld().equals(player.getWorld()) ||
                emptyBottleChestLoc.distanceSquared(player.getLocation()) > 4096.0) { // 64^2
            player.sendActionBar(Component.text("⚠️ Brewing Paused — you are too far away (> 64 blocks)!").color(NamedTextColor.RED));
            return;
        }

        // Active brewing action bar status
        player.sendActionBar(Component.text("🧪 HereAlky Auto-Brewing Active... [Manual Fuel required]").color(NamedTextColor.GOLD));

        // Locate mapped box block states
        Block emptyBottleBlock = emptyBottleChestLoc.getBlock();
        Block ingredientBlock = setupConfig.getIngredientBox().getBlock();
        Block outputBlock = setupConfig.getOutputBox().getBlock();

        if (!(emptyBottleBlock.getState() instanceof Container emptyBottleChest) ||
            !(ingredientBlock.getState() instanceof Container ingredientChest) ||
            !(outputBlock.getState() instanceof Container outputChest)) {
            stopTask("One of your mapped setup chests was broken or missing!", NamedTextColor.RED);
            return;
        }

        BrewRecipe recipe = brewConfig.getSelectedRecipe();
        if (recipe == null) {
            stopTask("No active recipe selected! Configure one with '/ha config'.", NamedTextColor.RED);
            return;
        }

        List<Location> standLocations = brewConfig.getRegisteredStands();
        if (standLocations.isEmpty()) {
            stopTask("No registered brewing stands! Please select stands under '/ha config'.", NamedTextColor.RED);
            return;
        }

        // Scan and populate empty/available stands
        for (Location loc : standLocations) {
            Block standBlock = loc.getBlock();
            if (standBlock.getType() != Material.BREWING_STAND) {
                continue; // Skip if stand was broken in-game
            }

            if (standBlock.getState() instanceof BrewingStand stand) {
                BrewerInventory inv = stand.getInventory();

                // Check if stand is completely empty (ready for a new Stage 1 batch)
                boolean hasPotions = false;
                for (int s = 0; s < 3; s++) {
                    ItemStack item = inv.getItem(s);
                    if (item != null && item.getType() != Material.AIR) {
                        hasPotions = true;
                        break;
                    }
                }
                ItemStack ingredient = inv.getItem(3);
                boolean hasIngredient = ingredient != null && ingredient.getType() != Material.AIR;

                if (!hasPotions && !hasIngredient) {
                    // Check Blaze Powder fuel (Manual Fueling required)
                    if (stand.getFuelLevel() == 0) {
                        // Skip stand as requested! Player must fuel it themselves
                        continue;
                    }

                    // Auto-Stop Check: Verify empty bottles are present
                    int bottleCount = countItemsInChest(emptyBottleChest, Material.GLASS_BOTTLE);
                    if (bottleCount == 0) {
                        stopTask("Empty bottles depleted in Empty Bottle Box!", NamedTextColor.YELLOW);
                        return;
                    }

                    // Auto-Stop Check: Verify Stage 1 ingredients are present
                    Material s1Ing = recipe.getStage1Ingredient();
                    int ingCount = countItemsInChest(ingredientChest, s1Ing);
                    if (ingCount == 0) {
                        stopTask("Recipe ingredients depleted: " + formatMatName(s1Ing) + " is missing from Ingredient Box!", NamedTextColor.YELLOW);
                        return;
                    }

                    // Auto-Stop Check: Verify Output Box has space for at least 3 completed potions
                    if (!hasSpaceForPotions(outputChest, 3)) {
                        stopTask("Your Output Box is completely full!", NamedTextColor.YELLOW);
                        return;
                    }

                    // Perform extraction and placement
                    int toTake = Math.min(3, bottleCount);
                    removeItemsFromChest(emptyBottleChest, Material.GLASS_BOTTLE, toTake);
                    removeItemsFromChest(ingredientChest, s1Ing, 1);

                    // Place 3 filled water bottles
                    for (int s = 0; s < toTake; s++) {
                        inv.setItem(s, createWaterBottle());
                    }

                    // Place Stage 1 ingredient
                    inv.setItem(3, new ItemStack(s1Ing, 1));
                    
                    player.playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1.1f);
                }
            }
        }
    }

    private int countItemsInChest(Container container, Material material) {
        int count = 0;
        Inventory inv = container.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItemsFromChest(Container container, Material material, int amount) {
        Inventory inv = container.getInventory();
        int remaining = amount;
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inv.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
    }

    private boolean hasSpaceForPotions(Container container, int count) {
        int emptySlots = 0;
        Inventory inv = container.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return emptySlots >= count;
    }

    private ItemStack createWaterBottle() {
        ItemStack bottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bottle.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.WATER);
            bottle.setItemMeta(meta);
        }
        return bottle;
    }

    private String formatMatName(Material mat) {
        if (mat == null) return "None";
        String name = mat.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
