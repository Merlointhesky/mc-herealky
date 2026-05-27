package com.herealky.herealky.task;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrewTaskManager {

    private final Map<UUID, BrewTask> activeTasks = new HashMap<>();

    public void startTask(Player player, BrewTask task) {
        stopTask(player, "Starting a new batch process.", net.kyori.adventure.text.format.NamedTextColor.YELLOW);
        activeTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(task.getPlugin(), 0L, 20L); // Check every second (20 ticks)
    }

    public void stopTask(Player player, String reason, net.kyori.adventure.text.format.NamedTextColor color) {
        BrewTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.stopTask(reason, color);
        }
    }

    public void removeActiveTask(UUID playerId) {
        activeTasks.remove(playerId);
    }

    public boolean isBrewing(Player player) {
        return activeTasks.containsKey(player.getUniqueId());
    }

    public BrewTask getActiveTask(Player player) {
        return activeTasks.get(player.getUniqueId());
    }

    public void stopAllTasks() {
        for (BrewTask task : activeTasks.values()) {
            try {
                task.cancel();
            } catch (Exception e) {}
        }
        activeTasks.clear();
    }
}
