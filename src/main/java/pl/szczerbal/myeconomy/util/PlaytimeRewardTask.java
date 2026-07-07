package pl.szczerbal.myeconomy.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.szczerbal.myeconomy.MyEconomy;
import pl.szczerbal.myeconomy.config.ConfigManager.PlaytimeRewardTier;

import java.util.List;

public class PlaytimeRewardTask {

    private final MyEconomy plugin;
    private BukkitTask task;

    public PlaytimeRewardTask(MyEconomy plugin) {
        this.plugin = plugin;
    }

    public void start() {
        long intervalTicks = plugin.getConfigManager().getPlaytimeRewardsInterval() * 60L * 20L;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<PlaytimeRewardTier> tiers = plugin.getConfigManager().getPlaytimeRewardTiers();
            if (tiers.isEmpty()) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlaytimeRewardTier matched = findBestTier(player, tiers);
                if (matched == null) continue;

                double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

                double reward;
                if (matched.type().equalsIgnoreCase("percent")) {
                    if (balance < matched.minBalance()) continue;
                    reward = balance * (matched.amount() / 100.0);
                } else {
                    reward = matched.amount();
                }

                if (reward <= 0) continue;

                if (matched.maxReward() > 0 && reward > matched.maxReward()) {
                    reward = matched.maxReward();
                }

                reward = Math.round(reward * 100.0) / 100.0;

                plugin.getEconomyManager().deposit(player.getUniqueId(), reward);

                player.sendMessage(plugin.getLangManager().getMessage("playtime.received",
                        "{amount}", plugin.getEconomyManager().formatBalance(reward),
                        "{currency}", plugin.getEconomyManager().getCurrencyName(reward),
                        "{tier}", matched.name()));

                if (plugin.getConfigManager().isLogPlaytimeRewards()) {
                    plugin.getTransactionLogger().log("PLAYTIME_REWARD", player.getName()
                            + " | Tier: " + matched.name()
                            + " | Amount: " + plugin.getEconomyManager().formatAmount(reward)
                            + " | Balance: " + plugin.getEconomyManager().formatAmount(balance + reward));
                }
            }
        }, intervalTicks, intervalTicks);
    }

    private PlaytimeRewardTier findBestTier(Player player, List<PlaytimeRewardTier> tiers) {
        for (PlaytimeRewardTier tier : tiers) {
            if (tier.isDefault() || player.hasPermission(tier.permission())) {
                return tier;
            }
        }
        return null;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
