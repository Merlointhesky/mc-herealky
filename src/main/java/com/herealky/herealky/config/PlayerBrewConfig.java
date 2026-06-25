package com.herealky.herealky.config;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class PlayerBrewConfig {

    private final String playerId;
    private BrewRecipe selectedRecipe;
    private List<Location> registeredStands = new ArrayList<>();
    private long lastModified;
    private int potionsBrewed = 0;

    public PlayerBrewConfig(String playerId) {
        this.playerId = playerId;
        this.lastModified = System.currentTimeMillis();
    }

    public String getPlayerId() {
        return playerId;
    }

    public BrewRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(BrewRecipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
        this.lastModified = System.currentTimeMillis();
    }

    public List<Location> getRegisteredStands() {
        return registeredStands;
    }

    public void setRegisteredStands(List<Location> registeredStands) {
        this.registeredStands = registeredStands != null ? registeredStands : new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getPotionsBrewed() {
        return potionsBrewed;
    }

    public void setPotionsBrewed(int potionsBrewed) {
        this.potionsBrewed = potionsBrewed;
        this.lastModified = System.currentTimeMillis();
    }
}
