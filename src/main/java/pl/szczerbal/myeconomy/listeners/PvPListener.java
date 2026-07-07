package pl.szczerbal.myeconomy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import pl.szczerbal.myeconomy.MyEconomy;

public class PvPListener implements Listener {

    private final MyEconomy plugin;

    public PvPListener(MyEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().isPvpMoneyEnabled()) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        double victimBalance = plugin.getEconomyManager().getBalance(victim.getUniqueId());
        double stealPercent = plugin.getConfigManager().getPvpStealPercent();
        double minimum = plugin.getConfigManager().getPvpMinimum();
        double maximum = plugin.getConfigManager().getPvpMaximum();

        double stolen = victimBalance * (stealPercent / 100.0);
        stolen = Math.round(stolen * 100.0) / 100.0;

        if (stolen < minimum) return;
        if (maximum > 0 && stolen > maximum) stolen = maximum;

        if (plugin.getConfigManager().isPvpVictimLoses()) {
            plugin.getEconomyManager().withdraw(victim.getUniqueId(), stolen);
        }
        plugin.getEconomyManager().deposit(killer.getUniqueId(), stolen);

        killer.sendMessage(plugin.getLangManager().getMessage("pvp.killer-gained",
                "{amount}", plugin.getEconomyManager().formatBalance(stolen),
                "{currency}", plugin.getEconomyManager().getCurrencyName(stolen),
                "{victim}", victim.getName()));

        plugin.getNotificationManager().showGain(killer, stolen);
        plugin.getNotificationManager().showLoss(victim, stolen);

        victim.sendMessage(plugin.getLangManager().getMessage("pvp.victim-lost",
                "{amount}", plugin.getEconomyManager().formatBalance(stolen),
                "{currency}", plugin.getEconomyManager().getCurrencyName(stolen),
                "{killer}", killer.getName()));

        if (plugin.getConfigManager().isLogPvp()) {
            plugin.getTransactionLogger().log("PVP", killer.getName() + " killed " + victim.getName()
                    + " | Stolen: " + plugin.getEconomyManager().formatAmount(stolen));
        }
    }
}
