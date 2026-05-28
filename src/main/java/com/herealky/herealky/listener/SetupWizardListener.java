package com.herealky.herealky.listener;

import com.herealky.herealky.command.SetupWizardCommand;
import com.herealky.herealky.setup.SetupConfiguration;
import com.herealky.herealky.setup.SetupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.UUID;

public class SetupWizardListener implements Listener {

    private final SetupManager setupManager;
    private final SetupWizardCommand wizardCommand;

    public SetupWizardListener(SetupManager setupManager, SetupWizardCommand wizardCommand) {
        this.setupManager = setupManager;
        this.wizardCommand = wizardCommand;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!setupManager.isInSetup(uuid)) {
            return;
        }

        // Strictly enforce Shift+Right Click
        if (!player.isSneaking()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        // Verify holding Water Bottle
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() != Material.POTION) {
            return;
        }
        if (!(held.getItemMeta() instanceof PotionMeta meta)) {
            return;
        }
        if (meta.getBasePotionType() != PotionType.WATER) {
            return;
        }

        event.setCancelled(true);
        wizardCommand.updateActivity(uuid);
        Location loc = block.getLocation();
        int step = setupManager.getCurrentStep(uuid);

        if (step >= 0 && step <= 2) {
            // Container checks for chest boxes
            if (!(block.getState() instanceof Container)) {
                player.sendMessage(Component.text("❌ Invalid block! Please click a chest, barrel, or other storage container.").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }

            if (step == 0) {
                setupManager.setIngredientBox(uuid, loc);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
                player.sendMessage(Component.text("✔ Ingredient Box set successfully!").color(NamedTextColor.GREEN));
                player.sendMessage(Component.text("  Step 2: Right-Click the Empty Bottle Box (where empty glass bottles are stored).")
                        .color(NamedTextColor.GREEN));
            } else if (step == 1) {
                setupManager.setEmptyBottleBox(uuid, loc);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
                player.sendMessage(Component.text("✔ Empty Bottle Box set successfully!").color(NamedTextColor.GREEN));
                player.sendMessage(Component.text("  Step 3: Right-Click the Output Box (where completed potions will be deposited).")
                        .color(NamedTextColor.GREEN));
            } else if (step == 2) {
                setupManager.setOutputBox(uuid, loc);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
                player.sendMessage(Component.text("✔ Output Box set successfully!").color(NamedTextColor.GREEN));
                player.sendMessage(Component.text("  Step 4: Right-Click your Water Source block (water source block, waterlogged block, or cauldron).")
                        .color(NamedTextColor.GREEN));
            }
        } else if (step == 3) {
            // Smart water check
            Location waterLoc = setupManager.findWaterSource(loc);
            if (waterLoc == null) {
                player.sendMessage(Component.text("❌ Smart scan failed: Clicked block is not a water block/cauldron, and has no adjacent water. Try again!").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }

            setupManager.setWaterSource(uuid, waterLoc);
            wizardCommand.removeActivity(uuid);

            SetupConfiguration config = setupManager.getSetupConfig(uuid);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("   Setup Configuration Complete!   ").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("✔ Ingredient Box: " + formatLoc(config.getIngredientBox())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("✔ Empty Bottle Box: " + formatLoc(config.getEmptyBottleBox())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("✔ Output Box: " + formatLoc(config.getOutputBox())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("✔ Water Source: " + formatLoc(config.getWaterSource())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("  Mapping is now fully complete! Set your recipe under '/ha config' next!").color(NamedTextColor.GREEN));
        }
    }

    private String formatLoc(org.bukkit.Location loc) {
        if (loc == null) return "None";
        return "[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
    }
}
