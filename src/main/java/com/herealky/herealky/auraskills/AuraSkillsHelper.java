package com.herealky.herealky.auraskills;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AuraSkillsHelper {

    private boolean auraSkillsAvailable = false;

    public void init() {
        auraSkillsAvailable = Bukkit.getPluginManager().getPlugin("AuraSkills") != null;
    }

    public boolean isAvailable() {
        return auraSkillsAvailable;
    }

    public int getAlchemyLevel(Player player) {
        if (!auraSkillsAvailable) return 0;
        try {
            AuraSkillsApi api = AuraSkillsApi.get();
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user != null) {
                return user.getSkillLevel(Skills.ALCHEMY);
            }
        } catch (Throwable e) {
            // Soft-depend safety
        }
        return 0;
    }

    public double getAlchemyXp(Player player) {
        if (!auraSkillsAvailable) return 0.0;
        try {
            AuraSkillsApi api = AuraSkillsApi.get();
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user != null) {
                return user.getSkillXp(Skills.ALCHEMY);
            }
        } catch (Throwable e) {
            // Soft-depend safety
        }
        return 0.0;
    }

    public void addAlchemyXp(Player player, double baseXp) {
        if (!auraSkillsAvailable) return;
        try {
            AuraSkillsApi api = AuraSkillsApi.get();
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user != null) {
                int level = getAlchemyLevel(player);
                double wisdom = 0.0;
                try {
                    wisdom = user.getStatLevel(Stats.WISDOM);
                } catch (Throwable t) {
                    // Fallback if Stats.WISDOM is not loaded or exists
                }
                
                // standard experience formula incorporating levels and Wisdom
                double xpAmount = baseXp * (1.0 + level * 0.02 + wisdom * 0.02);
                user.addSkillXp(Skills.ALCHEMY, xpAmount);
            }
        } catch (Throwable e) {
            // Soft-depend safety
        }
    }
}
