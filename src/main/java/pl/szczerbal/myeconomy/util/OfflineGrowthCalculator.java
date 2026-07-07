package pl.szczerbal.myeconomy.util;

import org.bukkit.entity.Player;
import pl.szczerbal.myeconomy.MyEconomy;

public class OfflineGrowthCalculator {

    private final MyEconomy plugin;

    public OfflineGrowthCalculator(MyEconomy plugin) {
        this.plugin = plugin;
    }

    public void processLoginGrowth(Player player) {
        if (!plugin.getConfigManager().isOfflineGrowthEnabled()) return;

        long lastLogout = plugin.getDatabaseManager().getLastLogout(player.getUniqueId());
        if (lastLogout <= 0) return;

        long now = System.currentTimeMillis();
        long offlineMillis = now - lastLogout;
        if (offlineMillis < 60_000) return;

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        double minBalance = plugin.getConfigManager().getOfflineGrowthMinBalance();
        if (balance < minBalance) return;

        int intervalMinutes = plugin.getConfigManager().getOfflineGrowthInterval();
        int maxHours = plugin.getConfigManager().getOfflineGrowthMaxHours();
        double ratePercent = plugin.getConfigManager().getOfflineGrowthRate();
        double maxGrowth = plugin.getConfigManager().getOfflineGrowthMaxGrowth();

        long maxOfflineMillis = maxHours * 3600_000L;
        long effectiveOffline = Math.min(offlineMillis, maxOfflineMillis);

        int intervals = (int) (effectiveOffline / (intervalMinutes * 60_000L));
        if (intervals <= 0) return;

        double growth = 0;
        double currentBalance = balance;
        for (int i = 0; i < intervals; i++) {
            double intervalGrowth = currentBalance * (ratePercent / 100.0);
            growth += intervalGrowth;
            currentBalance += intervalGrowth;
        }

        growth = Math.round(growth * 100.0) / 100.0;

        if (maxGrowth > 0 && growth > maxGrowth) {
            growth = maxGrowth;
        }

        if (growth <= 0) return;

        plugin.getEconomyManager().deposit(player.getUniqueId(), growth);

        double offlineHours = Math.round((effectiveOffline / 3600_000.0) * 10.0) / 10.0;

        player.sendMessage(plugin.getLangManager().getMessage("offline-growth.received",
                "{amount}", plugin.getEconomyManager().formatBalance(growth),
                "{currency}", plugin.getEconomyManager().getCurrencyName(growth),
                "{hours}", String.valueOf(offlineHours)));

        plugin.getNotificationManager().showGain(player, growth);

        if (plugin.getConfigManager().isLogOfflineGrowth()) {
            plugin.getTransactionLogger().log("OFFLINE_GROWTH", player.getName()
                    + " | Growth: " + plugin.getEconomyManager().formatAmount(growth)
                    + " | Offline: " + offlineHours + "h"
                    + " | Intervals: " + intervals);
        }
    }
}
