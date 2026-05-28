package com.herealky.herealky.listener;

import com.herealky.herealky.selection.SelectionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

public class SelectionListener implements Listener {

    private final SelectionManager selectionManager;

    public SelectionListener(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!selectionManager.isSelectionMode(uuid)) {
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
        Location loc = block.getLocation();

        Location locA = selectionManager.getPointA(uuid);
        Location locB = selectionManager.getPointB(uuid);

        if (locA == null || (locA != null && locB != null)) {
            selectionManager.setPointA(uuid, loc);
            selectionManager.setPointB(uuid, null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
            player.sendMessage(Component.text("✔ Bounding Point A set successfully: " + formatLoc(loc)).color(NamedTextColor.GREEN));
        } else {
            selectionManager.setPointB(uuid, loc);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f);
            player.sendMessage(Component.text("✔ Bounding Point B set successfully: " + formatLoc(loc)).color(NamedTextColor.GREEN));
        }
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "None";
        return "[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
    }
}
