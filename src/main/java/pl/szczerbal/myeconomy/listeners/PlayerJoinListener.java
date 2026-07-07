package pl.szczerbal.myeconomy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.szczerbal.myeconomy.MyEconomy;

public class PlayerJoinListener implements Listener {

    private final MyEconomy plugin;

    public PlayerJoinListener(MyEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getEconomyManager().createAccount(player.getUniqueId(), player.getName());

        plugin.getOfflineGrowthCalculator().processLoginGrowth(player);

        plugin.getNotificationManager().sendQueuedNotifications(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getDatabaseManager().setLastLogout(
                event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
