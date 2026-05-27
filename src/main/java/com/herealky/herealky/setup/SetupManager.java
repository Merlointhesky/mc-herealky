package com.herealky.herealky.setup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetupManager {

    private final Plugin plugin;
    private final File setupDir;
    private final Map<UUID, SetupConfiguration> configurations = new HashMap<>();
    private final Map<UUID, Integer> setupSteps = new HashMap<>();

    public SetupManager(Plugin plugin) {
        this.plugin = plugin;
        this.setupDir = new File(plugin.getDataFolder(), "setup-configs");
        if (!setupDir.exists()) {
            setupDir.mkdirs();
        }
    }

    public void startSetup(UUID playerId) {
        configurations.put(playerId, new SetupConfiguration(playerId.toString()));
        setupSteps.put(playerId, 0);
    }

    public int getCurrentStep(UUID playerId) {
        return setupSteps.getOrDefault(playerId, -1);
    }

    public boolean isInSetup(UUID playerId) {
        return setupSteps.containsKey(playerId) && setupSteps.get(playerId) >= 0;
    }

    public void setIngredientBox(UUID playerId, Location location) {
        if (!isInSetup(playerId)) return;
        SetupConfiguration config = configurations.get(playerId);
        config.setIngredientBox(location);
        setupSteps.put(playerId, 1);
    }

    public void setEmptyBottleBox(UUID playerId, Location location) {
        if (!isInSetup(playerId)) return;
        SetupConfiguration config = configurations.get(playerId);
        config.setEmptyBottleBox(location);
        setupSteps.put(playerId, 2);
    }

    public void setOutputBox(UUID playerId, Location location) {
        if (!isInSetup(playerId)) return;
        SetupConfiguration config = configurations.get(playerId);
        config.setOutputBox(location);
        setupSteps.put(playerId, 3);
    }

    public void setWaterSource(UUID playerId, Location location) {
        if (!isInSetup(playerId)) return;
        SetupConfiguration config = configurations.get(playerId);
        config.setWaterSource(location);
        completeSetup(playerId);
    }

    public void completeSetup(UUID playerId) {
        SetupConfiguration config = configurations.get(playerId);
        if (config != null && config.isComplete()) {
            setupSteps.remove(playerId);
            saveConfiguration(playerId);
        }
    }

    public void cancelSetup(UUID playerId) {
        configurations.remove(playerId);
        setupSteps.remove(playerId);
    }

    public SetupConfiguration getSetupConfig(UUID playerId) {
        if (!configurations.containsKey(playerId)) {
            loadConfiguration(playerId);
        }
        return configurations.get(playerId);
    }

    public boolean hasSetupConfig(UUID playerId) {
        SetupConfiguration config = getSetupConfig(playerId);
        return config != null && config.isComplete();
    }

    public void clearSetupConfig(UUID playerId) {
        configurations.remove(playerId);
        setupSteps.remove(playerId);
        File file = new File(setupDir, playerId + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    public void saveConfiguration(UUID playerId) {
        SetupConfiguration config = configurations.get(playerId);
        if (config == null) return;

        File file = new File(setupDir, playerId + ".yml");
        FileConfiguration yaml = new YamlConfiguration();

        yaml.set("playerId", config.getPlayerId());
        yaml.set("ingredientBox", config.getIngredientBox());
        yaml.set("emptyBottleBox", config.getEmptyBottleBox());
        yaml.set("outputBox", config.getOutputBox());
        yaml.set("waterSource", config.getWaterSource());
        yaml.set("createdAt", config.getCreatedAt());
        yaml.set("lastModified", config.getLastModified());

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save setup configuration for " + playerId + ": " + e.getMessage());
        }
    }

    public void loadConfiguration(UUID playerId) {
        File file = new File(setupDir, playerId + ".yml");
        if (!file.exists()) return;

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        SetupConfiguration config = new SetupConfiguration(playerId.toString());

        config.setIngredientBox(yaml.getLocation("ingredientBox"));
        config.setEmptyBottleBox(yaml.getLocation("emptyBottleBox"));
        config.setOutputBox(yaml.getLocation("outputBox"));
        config.setWaterSource(yaml.getLocation("waterSource"));
        config.setCreatedAt(yaml.getLong("createdAt", System.currentTimeMillis()));
        config.setLastModified(yaml.getLong("lastModified", System.currentTimeMillis()));

        configurations.put(playerId, config);
    }

    // Smart water source scanning logic
    public Location findWaterSource(Location clicked) {
        if (isWater(clicked.getBlock())) {
            return clicked;
        }
        
        Block block = clicked.getBlock();
        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace face : faces) {
            Block adj = block.getRelative(face);
            if (isWater(adj)) {
                return adj.getLocation();
            }
        }
        return null;
    }

    private boolean isWater(Block block) {
        if (block == null) return false;
        Material type = block.getType();
        if (type == Material.WATER || type == Material.CAULDRON) {
            return true;
        }
        if (block.getBlockData() instanceof Waterlogged wl) {
            return wl.isWaterlogged();
        }
        return false;
    }
}
