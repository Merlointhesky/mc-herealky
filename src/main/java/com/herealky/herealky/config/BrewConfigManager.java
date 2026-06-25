package com.herealky.herealky.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrewConfigManager {

    private final Plugin plugin;
    private final File configDir;
    private final Map<UUID, PlayerBrewConfig> configs = new HashMap<>();

    public BrewConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configDir = new File(plugin.getDataFolder(), "brew-configs");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public PlayerBrewConfig getPlayerConfig(UUID playerId) {
        return configs.computeIfAbsent(playerId, id -> {
            PlayerBrewConfig config = new PlayerBrewConfig(id.toString());
            loadConfiguration(id, config);
            return config;
        });
    }

    public void saveConfiguration(UUID playerId) {
        PlayerBrewConfig config = configs.get(playerId);
        if (config == null) return;

        File file = new File(configDir, playerId + ".yml");
        FileConfiguration yaml = new YamlConfiguration();

        yaml.set("playerId", config.getPlayerId());
        yaml.set("selectedRecipe", config.getSelectedRecipe() != null ? config.getSelectedRecipe().name() : null);
        
        List<Location> stands = config.getRegisteredStands();
        yaml.set("registeredStands", stands);
        yaml.set("lastModified", config.getLastModified());
        yaml.set("potionsBrewed", config.getPotionsBrewed());

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save brew configuration for " + playerId + ": " + e.getMessage());
        }
    }

    public void loadConfiguration(UUID playerId, PlayerBrewConfig config) {
        File file = new File(configDir, playerId + ".yml");
        if (!file.exists()) return;

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        
        String recipeStr = yaml.getString("selectedRecipe");
        if (recipeStr != null) {
            try {
                config.setSelectedRecipe(BrewRecipe.valueOf(recipeStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown recipe: " + recipeStr + " for player " + playerId);
            }
        }

        List<?> list = yaml.getList("registeredStands");
        List<Location> stands = new ArrayList<>();
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Location loc) {
                    stands.add(loc);
                }
            }
        }
        config.setRegisteredStands(stands);
        config.setLastModified(yaml.getLong("lastModified", System.currentTimeMillis()));
        config.setPotionsBrewed(yaml.getInt("potionsBrewed", 0));
    }
}
