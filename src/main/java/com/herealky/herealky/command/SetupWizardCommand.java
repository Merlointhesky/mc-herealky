package com.herealky.herealky.command;

import com.herealky.herealky.setup.SetupConfiguration;
import com.herealky.herealky.setup.SetupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetupWizardCommand implements Listener {

    private final SetupManager setupManager;
    private final Plugin plugin;
    private final Map<UUID, Long> lastActiveTime = new HashMap<>();

    public SetupWizardCommand(SetupManager setupManager, Plugin plugin) {
        this.setupManager = setupManager;
        this.plugin = plugin;
    }

    public void onCommand(Player player) {
        UUID uuid = player.getUniqueId();
        if (setupManager.isInSetup(uuid)) {
            player.sendMessage(Component.text("You are already in setup wizard!").color(NamedTextColor.YELLOW));
            return;
        }

        setupManager.startSetup(uuid);
        lastActiveTime.put(uuid, System.currentTimeMillis());

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("     HereAlky Setup Wizard     ").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("★ Let's configure your brewing station coordinates!").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  Step 1: Hold an Empty Glass Bottle and Right-Click the Ingredient Box (chest/barrel where ingredients for all stages are collected).")
                .color(NamedTextColor.GREEN));
        player.sendMessage(Component.text("  Type '/ha stop' to cancel setup.").color(NamedTextColor.GRAY));
    }

    public void checkTimeouts() {
        long now = System.currentTimeMillis();
        lastActiveTime.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            long lastActive = entry.getValue();
            if (now - lastActive > 300000) { // 5 minutes
                setupManager.cancelSetup(id);
                Player player = Bukkit.getPlayer(id);
                if (player != null && player.isOnline()) {
                    player.sendMessage(Component.text("HereAlky setup wizard timed out (5 minutes of inactivity).").color(NamedTextColor.RED));
                }
                return true;
            }
            return false;
        });
    }

    public void updateActivity(UUID uuid) {
        lastActiveTime.put(uuid, System.currentTimeMillis());
    }

    public void removeActivity(UUID uuid) {
        lastActiveTime.remove(uuid);
    }
}
