package com.herealky.herealky.setup;

import org.bukkit.Location;

public class SetupConfiguration {

    private final String playerId;
    private Location ingredientBox;
    private Location emptyBottleBox;
    private Location outputBox;
    private Location waterSource;
    private long createdAt;
    private long lastModified;

    public SetupConfiguration(String playerId) {
        this.playerId = playerId;
        this.createdAt = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }

    public String getPlayerId() {
        return playerId;
    }

    public Location getIngredientBox() {
        return ingredientBox;
    }

    public void setIngredientBox(Location ingredientBox) {
        this.ingredientBox = ingredientBox;
        this.lastModified = System.currentTimeMillis();
    }

    public Location getEmptyBottleBox() {
        return emptyBottleBox;
    }

    public void setEmptyBottleBox(Location emptyBottleBox) {
        this.emptyBottleBox = emptyBottleBox;
        this.lastModified = System.currentTimeMillis();
    }

    public Location getOutputBox() {
        return outputBox;
    }

    public void setOutputBox(Location outputBox) {
        this.outputBox = outputBox;
        this.lastModified = System.currentTimeMillis();
    }

    public Location getWaterSource() {
        return waterSource;
    }

    public void setWaterSource(Location waterSource) {
        this.waterSource = waterSource;
        this.lastModified = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isComplete() {
        return ingredientBox != null && emptyBottleBox != null && outputBox != null && waterSource != null;
    }
}
