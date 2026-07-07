package pl.szczerbal.myeconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.szczerbal.myeconomy.MyEconomy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    private final MyEconomy plugin;

    public BalanceCommand(MyEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("balance").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLangManager().getMessage("general.player-only"));
                return true;
            }
            if (!player.hasPermission("myeconomy.balance")) {
                player.sendMessage(plugin.getLangManager().getMessage("general.no-permission"));
                return true;
            }
            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            player.sendMessage(plugin.getLangManager().getMessage("balance.self",
                    "{balance}", plugin.getEconomyManager().formatBalance(balance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(balance)));
            return true;
        }

        if (!sender.hasPermission("myeconomy.balance.others")) {
            sender.sendMessage(plugin.getLangManager().getMessage("balance.no-permission"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.player-not-found",
                    "{player}", args[0]));
            return true;
        }

        double balance = plugin.getEconomyManager().getBalance(target.getUniqueId());
        sender.sendMessage(plugin.getLangManager().getMessage("balance.other",
                "{player}", target.getName(),
                "{balance}", plugin.getEconomyManager().formatBalance(balance),
                "{currency}", plugin.getEconomyManager().getCurrencyName(balance)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("myeconomy.balance.others")) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return List.of();
    }
}
