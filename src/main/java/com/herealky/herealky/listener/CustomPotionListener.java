package com.herealky.herealky.listener;

import com.herealky.herealky.HereAlkyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomPotionListener implements Listener {

    private final HereAlkyPlugin plugin;
    private final Map<UUID, Long> magnetismPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> trueSightPlayers = new ConcurrentHashMap<>();

    public CustomPotionListener(HereAlkyPlugin plugin) {
        this.plugin = plugin;
        startEffectTask();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        Component displayNameComp = item.getItemMeta().displayName();
        if (displayNameComp == null) return;
        
        String displayName = PlainTextComponentSerializer.plainText().serialize(displayNameComp);
        Player player = event.getPlayer();

        long durationMs = 3 * 60 * 1000L; // Normal 3 mins
        if (displayName.contains("Extended")) durationMs = 8 * 60 * 1000L;
        else if (displayName.contains("1-Hour")) durationMs = 60 * 60 * 1000L;

        if (displayName.contains("Magnetism")) {
            magnetismPlayers.put(player.getUniqueId(), System.currentTimeMillis() + durationMs);
            player.sendMessage(Component.text("🧲 You feel a strong magnetic pull..."));
        } else if (displayName.contains("True Sight")) {
            trueSightPlayers.put(player.getUniqueId(), System.currentTimeMillis() + durationMs);
            player.sendMessage(Component.text("👁 Your vision penetrates the darkness..."));
        }
    }

    private void startEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                // Process Magnetism
                for (Map.Entry<UUID, Long> entry : magnetismPlayers.entrySet()) {
                    if (now > entry.getValue()) {
                        magnetismPlayers.remove(entry.getKey());
                        continue;
                    }
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        for (Entity entity : player.getNearbyEntities(10.0, 10.0, 10.0)) {
                            if (entity instanceof Item itemEntity) {
                                // Pull item towards player
                                org.bukkit.util.Vector dir = player.getLocation().toVector().subtract(itemEntity.getLocation().toVector()).normalize();
                                itemEntity.setVelocity(dir.multiply(0.4));
                            }
                        }
                    }
                }

                // Process True Sight
                for (Map.Entry<UUID, Long> entry : trueSightPlayers.entrySet()) {
                    if (now > entry.getValue()) {
                        trueSightPlayers.remove(entry.getKey());
                        continue;
                    }
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        for (Entity entity : player.getNearbyEntities(32.0, 32.0, 32.0)) {
                            if (entity instanceof Monster monster) {
                                monster.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, false));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Run every 0.5 seconds
    }
}
