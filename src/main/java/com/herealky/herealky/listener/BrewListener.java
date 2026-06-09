package com.herealky.herealky.listener;

import com.herealky.herealky.HereAlkyPlugin;
import com.herealky.herealky.auraskills.AuraSkillsHelper;
import com.herealky.herealky.hereroleplay.HereRolePlayHelper;
import com.herealky.herealky.config.BrewConfigManager;
import com.herealky.herealky.config.BrewRecipe;
import com.herealky.herealky.config.PlayerBrewConfig;
import com.herealky.herealky.setup.SetupConfiguration;
import com.herealky.herealky.setup.SetupManager;
import com.herealky.herealky.task.BrewTask;
import com.herealky.herealky.task.BrewTaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.UUID;

public class BrewListener implements Listener {

    private final HereAlkyPlugin plugin;
    private final SetupManager setupManager;
    private final BrewConfigManager configManager;
    private final BrewTaskManager taskManager;
    private final AuraSkillsHelper auraSkillsHelper;
    private final HereRolePlayHelper hereRolePlayHelper;

    public BrewListener(HereAlkyPlugin plugin) {
        this.plugin = plugin;
        this.setupManager = plugin.getSetupManager();
        this.configManager = plugin.getBrewConfigManager();
        this.taskManager = plugin.getBrewTaskManager();
        this.auraSkillsHelper = plugin.getAuraSkillsHelper();
        this.hereRolePlayHelper = plugin.getHereRolePlayHelper();
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        Location standLoc = event.getBlock().getLocation();

        // Find which active player config registers this brewing stand
        Player activePlayer = null;
        PlayerBrewConfig playerConfig = null;
        SetupConfiguration setupConfig = null;

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerBrewConfig c = configManager.getPlayerConfig(p.getUniqueId());
            if (c.getRegisteredStands().contains(standLoc)) {
                activePlayer = p;
                playerConfig = c;
                setupConfig = setupManager.getSetupConfig(p.getUniqueId());
                break;
            }
        }

        if (activePlayer == null || playerConfig == null || setupConfig == null) {
            return; // Not an active automated stand
        }

        final Player player = activePlayer;
        final PlayerBrewConfig config = playerConfig;
        final SetupConfiguration setup = setupConfig;
        final BrewRecipe recipe = config.getSelectedRecipe();

        if (recipe == null) return;

        // Schedule delayed check (1 tick later) to allow the vanilla item conversion to complete
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (event.getBlock().getType() != Material.BREWING_STAND) return;
            
            BrewingStand stand = (BrewingStand) event.getBlock().getState();
            BrewerInventory inv = stand.getInventory();
            BrewTask task = taskManager.getActiveTask(player);

            // Fetch the potion in slot 0 to identify the completed stage
            ItemStack potionStack = inv.getItem(0);
            if (potionStack == null || potionStack.getType() != Material.POTION) return;

            PotionMeta meta = (PotionMeta) potionStack.getItemMeta();
            if (meta == null) return;

            PotionType type = meta.getBasePotionType();
            if (type == null) return;

            // Ingredient Chest box check
            Location ingredientChestLoc = setup.getIngredientBox();
            if (ingredientChestLoc == null || !(ingredientChestLoc.getBlock().getState() instanceof Container ingredientChest)) {
                if (task != null) task.stopTask("Ingredient Box was missing or broken!", NamedTextColor.RED);
                return;
            }

            // Output Chest box check
            Location outputChestLoc = setup.getOutputBox();
            if (outputChestLoc == null || !(outputChestLoc.getBlock().getState() instanceof Container outputChest)) {
                if (task != null) task.stopTask("Output Box was missing or broken!", NamedTextColor.RED);
                return;
            }

            // Process stage completion
            if (type == PotionType.AWKWARD) {
                // STAGE 1 COMPLETED!
                awardStageXp(player, recipe, 1);

                if (recipe.getStepsCount() == 1) {
                    // 1-Stage Potion (e.g. Weakness) - Batch Complete!
                    depositPotionsAndComplete(inv, outputChest, player, task, 1);
                } else {
                    // Feed Stage 2 Ingredient
                    Material s2Mat = recipe.getStage2Ingredient();
                    int count = countItemsInChest(ingredientChest, s2Mat);
                    if (count == 0) {
                        if (task != null) {
                            task.stopTask("Recipe ingredients depleted: " + formatMatName(s2Mat) + " is missing from Ingredient Box!", NamedTextColor.YELLOW);
                        }
                        return;
                    }

                    removeItemsFromChest(ingredientChest, s2Mat, 1);
                    inv.setItem(3, new ItemStack(s2Mat, 1));
                    player.playSound(standLoc, Sound.BLOCK_BREWING_STAND_BREW, 0.6f, 1.2f);
                }
            } else if (type == recipe.getBasePotionType() && recipe.getStepsCount() == 3) {
                // STAGE 2 COMPLETED! (In a 3-Stage recipe, we've completed the base effect potion, preparing modifier)
                awardStageXp(player, recipe, 2);

                Material s3Mat = recipe.getStage3Ingredient();
                int count = countItemsInChest(ingredientChest, s3Mat);
                if (count == 0) {
                    if (task != null) {
                        task.stopTask("Recipe ingredients depleted: " + formatMatName(s3Mat) + " is missing from Ingredient Box!", NamedTextColor.YELLOW);
                    }
                    return;
                }

                removeItemsFromChest(ingredientChest, s3Mat, 1);
                inv.setItem(3, new ItemStack(s3Mat, 1));
                player.playSound(standLoc, Sound.BLOCK_BREWING_STAND_BREW, 0.6f, 1.2f);
            } else {
                // BATCH COMPLETE! (This is either Stage 2 finish for a 2-Stage potion, or Stage 3 finish for a 3-Stage potion)
                int stageCompleted = recipe.getStepsCount();
                awardStageXp(player, recipe, stageCompleted);
                depositPotionsAndComplete(inv, outputChest, player, task, 3);
            }
        });
    }

    private void awardStageXp(Player player, BrewRecipe recipe, int stage) {
        double baseXp = recipe.getStageXp(stage);
        if (baseXp <= 0.0) return;

        // Award Vanilla XP (0.3 XP per bottle, approx 1 XP per stage batch)
        player.giveExp(1);

        // Award AuraSkills Alchemy XP
        if (auraSkillsHelper.isAvailable()) {
            auraSkillsHelper.addAlchemyXp(player, baseXp);
        }
        
        // Award HereRolePlay Craft XP
        if (hereRolePlayHelper.isAvailable()) {
            hereRolePlayHelper.addCraftXp(player, baseXp);
        }

        player.sendActionBar(Component.text("🧪 Stage " + stage + " Complete! +" + baseXp + " Alchemy XP").color(NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }

    private void depositPotionsAndComplete(BrewerInventory inv, Container outputChest, Player player, BrewTask task, int potionCount) {
        Inventory chestInv = outputChest.getInventory();
        int moved = 0;

        for (int s = 0; s < 3; s++) {
            ItemStack item = inv.getItem(s);
            if (item != null && item.getType() != Material.AIR) {
                chestInv.addItem(item);
                inv.setItem(s, null);
                moved++;
            }
        }

        if (moved > 0) {
            player.playSound(outputChest.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.1f);
            if (task != null) {
                task.incrementPotionsCompleted(moved);
            }
            player.sendMessage(Component.text("✔ Batch completed! " + moved + " potions deposited in Output Box.").color(NamedTextColor.GREEN));
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

    private String formatMatName(Material mat) {
        if (mat == null) return "None";
        String name = mat.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
