package pl.szczerbal.myeconomy.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.szczerbal.myeconomy.MyEconomy;
import pl.szczerbal.myeconomy.config.LangManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MoneyNotificationManager {

    private final MyEconomy plugin;
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    public MoneyNotificationManager(MyEconomy plugin) {
        this.plugin = plugin;
    }

    public void showGain(Player player, double amount) {
        if (!plugin.getConfigManager().isActionbarEnabled()) return;
        String format = plugin.getConfigManager().getActionbarGainFormat()
                .replace("{amount}", plugin.getEconomyManager().formatBalance(amount));
        showActionBar(player, format);
    }

    public void showLoss(Player player, double amount) {
        if (!plugin.getConfigManager().isActionbarEnabled()) return;
        String format = plugin.getConfigManager().getActionbarLossFormat()
                .replace("{amount}", plugin.getEconomyManager().formatBalance(amount));
        showActionBar(player, format);
    }

    private void showActionBar(Player player, String text) {
        Component component = LangManager.colorize(text);
        player.sendActionBar(component);

        BukkitTask existing = activeTasks.remove(player.getUniqueId());
        if (existing != null) existing.cancel();

        int duration = plugin.getConfigManager().getActionbarDuration();
        int repeats = duration / 20;

        if (repeats > 1) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count >= repeats || !player.isOnline()) {
                        BukkitTask t = activeTasks.remove(player.getUniqueId());
                        if (t != null) t.cancel();
                        return;
                    }
                    player.sendActionBar(component);
                    count++;
                }
            }, 20, 20);
            activeTasks.put(player.getUniqueId(), task);
        }
    }

    public void queueOfflineNotification(UUID uuid, String message) {
        if (!plugin.getConfigManager().isOfflineQueueEnabled()) return;
        plugin.getDatabaseManager().addOfflineNotification(uuid, message);
    }

    public void sendQueuedNotifications(Player player) {
        if (!plugin.getConfigManager().isOfflineQueueEnabled()) return;

        List<String> messages = plugin.getDatabaseManager().getOfflineNotifications(player.getUniqueId());
        if (messages.isEmpty()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(plugin.getLangManager().getMessage("offline-notifications.header"));
            for (String msg : messages) {
                player.sendMessage(LangManager.colorize(msg));
            }
            player.sendMessage(plugin.getLangManager().getMessage("offline-notifications.footer",
                    "{count}", String.valueOf(messages.size())));

            plugin.getDatabaseManager().clearOfflineNotifications(player.getUniqueId());
        }, 40);
    }
}
