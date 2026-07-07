package pl.szczerbal.myeconomy.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.szczerbal.myeconomy.MyEconomy;

public class InterestTask {

    private final MyEconomy plugin;
    private BukkitTask task;

    public InterestTask(MyEconomy plugin) {
        this.plugin = plugin;
    }

    public void start() {
        long intervalTicks = plugin.getConfigManager().getInterestInterval() * 60L * 20L;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            double rate = plugin.getConfigManager().getInterestRate() / 100.0;
            double maxBalance = plugin.getConfigManager().getInterestMaxBalance();
            double minBalance = plugin.getConfigManager().getInterestMinBalance();

            for (Player player : Bukkit.getOnlinePlayers()) {
                double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

                if (balance < minBalance || balance > maxBalance) continue;

                double interest = balance * rate;
                if (interest <= 0) continue;

                interest = Math.round(interest * 100.0) / 100.0;

                plugin.getEconomyManager().deposit(player.getUniqueId(), interest);

                player.sendMessage(plugin.getLangManager().getMessage("interest.received",
                        "{amount}", plugin.getEconomyManager().formatBalance(interest),
                        "{currency}", plugin.getEconomyManager().getCurrencyName(interest)));

                if (plugin.getConfigManager().isLogInterest()) {
                    plugin.getTransactionLogger().log("INTEREST", player.getName()
                            + " | Amount: " + plugin.getEconomyManager().formatAmount(interest)
                            + " | Balance: " + plugin.getEconomyManager().formatAmount(balance + interest));
                }
            }
        }, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
