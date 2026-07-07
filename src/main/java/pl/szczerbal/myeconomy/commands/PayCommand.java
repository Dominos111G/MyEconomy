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

public class PayCommand implements CommandExecutor, TabCompleter {

    private final MyEconomy plugin;

    public PayCommand(MyEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("pay").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.player-only"));
            return true;
        }

        if (!player.hasPermission("myeconomy.pay")) {
            player.sendMessage(plugin.getLangManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/pay <player> <amount>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getLangManager().getMessage("general.player-not-found",
                    "{player}", args[0]));
            return true;
        }

        if (!plugin.getConfigManager().isAllowSelfPay() && target.equals(player)) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.self"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.invalid-amount"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.invalid-amount"));
            return true;
        }

        if (amount < plugin.getConfigManager().getPayMinimum()) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.minimum",
                    "{minimum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getPayMinimum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getPayMinimum())));
            return true;
        }

        if (amount > plugin.getConfigManager().getPayMaximum()) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.maximum",
                    "{maximum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getPayMaximum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getPayMaximum())));
            return true;
        }

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        if (balance < amount) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.not-enough",
                    "{balance}", plugin.getEconomyManager().formatBalance(balance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(balance)));
            return true;
        }

        double taxPercent = plugin.getConfigManager().getPayTaxPercent();
        double tax = amount * (taxPercent / 100.0);
        double received = amount - tax;

        plugin.getEconomyManager().withdraw(player.getUniqueId(), amount);
        plugin.getEconomyManager().deposit(target.getUniqueId(), received);

        if (tax > 0) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.success-tax",
                    "{amount}", plugin.getEconomyManager().formatBalance(received),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(received),
                    "{player}", target.getName(),
                    "{tax}", plugin.getEconomyManager().formatBalance(tax)));
        } else {
            player.sendMessage(plugin.getLangManager().getMessage("pay.success",
                    "{amount}", plugin.getEconomyManager().formatBalance(amount),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(amount),
                    "{player}", target.getName()));
        }

        plugin.getNotificationManager().showLoss(player, amount);

        if (target.isOnline()) {
            target.sendMessage(plugin.getLangManager().getMessage("pay.received",
                    "{amount}", plugin.getEconomyManager().formatBalance(received),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(received),
                    "{player}", player.getName()));
            plugin.getNotificationManager().showGain(target, received);
        } else {
            String offlineMsg = plugin.getLangManager().getRaw("pay.received")
                    .replace("{amount}", plugin.getEconomyManager().formatBalance(received))
                    .replace("{currency}", plugin.getEconomyManager().getCurrencyName(received))
                    .replace("{player}", player.getName());
            plugin.getNotificationManager().queueOfflineNotification(target.getUniqueId(), offlineMsg);
        }

        if (plugin.getConfigManager().isLogPay()) {
            plugin.getTransactionLogger().log("PAY", player.getName() + " -> " + target.getName()
                    + " | Amount: " + plugin.getEconomyManager().formatAmount(amount)
                    + (tax > 0 ? " | Tax: " + plugin.getEconomyManager().formatAmount(tax) : ""));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input) && !player.equals(sender)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        if (args.length == 2) {
            return List.of("100", "500", "1000");
        }
        return List.of();
    }
}
