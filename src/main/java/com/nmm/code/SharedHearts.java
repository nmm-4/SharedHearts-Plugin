package com.nmm.code;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class SharedHearts extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Optional<? extends Player> curr = Bukkit.getOnlinePlayers().stream().findFirst();

        if (curr.isPresent()) {
            Player other = curr.get();
            if (other.identity().equals(player.identity())) return;

            player.setHealth(other.getHealth());
            player.setAbsorptionAmount(other.getAbsorptionAmount());
            player.setFoodLevel(other.getFoodLevel());
        }

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) return;

        double damage = event.getFinalDamage();
        double absorption = damaged.getAbsorptionAmount();
        double remainingDamage = Math.max(0, damage - absorption);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(damaged)) continue;

            double pAbs = p.getAbsorptionAmount();
            double newAbs = Math.max(0, pAbs - damage);
            p.setAbsorptionAmount(newAbs);

            if (remainingDamage > 0) {
                p.setHealth(Math.max(0, p.getHealth() - remainingDamage));
            }

            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
        }

        String message =
                ChatColor.DARK_GRAY + "[" +
                ChatColor.YELLOW + "SharedHealth" +
                ChatColor.DARK_GRAY + "] " +
                ChatColor.WHITE + damaged.getName() + " " +
                ChatColor.GRAY + "has taken " +
                ChatColor.RED + String.format("%.1f", damage / 2) + " ❤ " +
                ChatColor.GRAY + "damage.";

        Bukkit.broadcastMessage(message);

    }
    @EventHandler
    public void onAbsorptionRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player healer)) return;

        double abs = healer.getAbsorptionAmount();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(healer)) {
                p.setAbsorptionAmount(abs);
            }
        }
    }


    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player healer)) return;
        double healAmount = event.getAmount();
        if (healAmount <= 0) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(healer)) {
                double newHealth = Math.min(p.getMaxHealth(), p.getHealth() + healAmount);
                p.setHealth(newHealth);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        int change = event.getFoodLevel();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                p.setFoodLevel(Math.max(0, Math.min(20, change)));
            }
        }
    }
}
