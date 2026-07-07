package pl.szczerbal.myeconomy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.szczerbal.myeconomy.MyEconomy;

import java.util.LinkedHashMap;
import java.util.Map;

public class BalanceTopCommand implements CommandExecutor {

    private final MyEconomy plugin;

    public BalanceTopCommand(MyEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("myeconomy.balancetop")) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.no-permission"));
            return true;
        }

        int entries = plugin.getConfigManager().getLeaderboardEntries();
        LinkedHashMap<String, Double> top = plugin.getEconomyManager().getTopBalances(entries);

        if (top.isEmpty()) {
            sender.sendMessage(plugin.getLangManager().getMessage("balancetop.empty"));
            return true;
        }

        String senderName = sender instanceof Player ? sender.getName() : null;

        sender.sendMessage(plugin.getLangManager().getMessage("balancetop.header"));
        sender.sendMessage(plugin.getLangManager().getMessage("balancetop.title",
                "{count}", String.valueOf(entries)));
        sender.sendMessage(plugin.getLangManager().getMessage("balancetop.separator"));

        int rank = 1;
        for (Map.Entry<String, Double> entry : top.entrySet()) {
            String name = entry.getKey();
            double balance = entry.getValue();
            String langKey = name.equals(senderName) ? "balancetop.entry-self" : "balancetop.entry";

            sender.sendMessage(plugin.getLangManager().getMessage(langKey,
                    "{rank}", String.valueOf(rank),
                    "{player}", name,
                    "{balance}", plugin.getEconomyManager().formatBalance(balance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(balance)));
            rank++;
        }

        if (sender instanceof Player player) {
            int playerRank = plugin.getEconomyManager().getRank(player.getUniqueId());
            if (playerRank > 0) {
                sender.sendMessage(plugin.getLangManager().getMessage("balancetop.your-rank",
                        "{rank}", String.valueOf(playerRank)));
            }
        }

        sender.sendMessage(plugin.getLangManager().getMessage("balancetop.footer"));
        return true;
    }
}
