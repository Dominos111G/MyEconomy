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

public class BankCommand implements CommandExecutor, TabCompleter {

    private final MyEconomy plugin;

    public BankCommand(MyEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("bank").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.player-only"));
            return true;
        }

        if (!plugin.getConfigManager().isBankEnabled()) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.disabled"));
            return true;
        }

        if (!player.hasPermission("myeconomy.bank")) {
            player.sendMessage(plugin.getLangManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            double bankBalance = plugin.getEconomyManager().getBankBalance(player.getUniqueId());
            player.sendMessage(plugin.getLangManager().getMessage("bank.balance",
                    "{balance}", plugin.getEconomyManager().formatBalance(bankBalance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(bankBalance)));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit" -> handleDeposit(player, args);
            case "withdraw" -> handleWithdraw(player, args);
            case "transfer" -> handleTransfer(player, args);
            default -> player.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/bank [deposit|withdraw|transfer] [amount|player]"));
        }

        return true;
    }

    private void handleDeposit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/bank deposit <amount>"));
            return;
        }

        double amount = parseAmount(player, args[1]);
        if (amount < 0) return;

        if (amount < plugin.getConfigManager().getBankMinimum()) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.minimum",
                    "{minimum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getBankMinimum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getBankMinimum())));
            return;
        }

        double walletBalance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        if (walletBalance < amount) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.not-enough-wallet",
                    "{balance}", plugin.getEconomyManager().formatBalance(walletBalance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(walletBalance)));
            return;
        }

        double maxBank = plugin.getConfigManager().getBankMaxBalance();
        double currentBank = plugin.getEconomyManager().getBankBalance(player.getUniqueId());
        if (maxBank > 0 && currentBank + amount > maxBank) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.max-reached",
                    "{max}", plugin.getEconomyManager().formatBalance(maxBank),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(maxBank)));
            return;
        }

        plugin.getEconomyManager().bankDeposit(player.getUniqueId(), amount);

        player.sendMessage(plugin.getLangManager().getMessage("bank.deposit-success",
                "{amount}", plugin.getEconomyManager().formatBalance(amount),
                "{currency}", plugin.getEconomyManager().getCurrencyName(amount)));

        plugin.getNotificationManager().showLoss(player, amount);

        if (plugin.getConfigManager().isLogBank()) {
            plugin.getTransactionLogger().log("BANK_DEPOSIT", player.getName()
                    + " | Amount: " + plugin.getEconomyManager().formatAmount(amount));
        }
    }

    private void handleWithdraw(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/bank withdraw <amount>"));
            return;
        }

        double amount = parseAmount(player, args[1]);
        if (amount < 0) return;

        if (amount < plugin.getConfigManager().getBankMinimum()) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.minimum",
                    "{minimum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getBankMinimum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getBankMinimum())));
            return;
        }

        double bankBalance = plugin.getEconomyManager().getBankBalance(player.getUniqueId());
        if (bankBalance < amount) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.not-enough-bank",
                    "{balance}", plugin.getEconomyManager().formatBalance(bankBalance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(bankBalance)));
            return;
        }

        plugin.getEconomyManager().bankWithdraw(player.getUniqueId(), amount);

        player.sendMessage(plugin.getLangManager().getMessage("bank.withdraw-success",
                "{amount}", plugin.getEconomyManager().formatBalance(amount),
                "{currency}", plugin.getEconomyManager().getCurrencyName(amount)));

        plugin.getNotificationManager().showGain(player, amount);

        if (plugin.getConfigManager().isLogBank()) {
            plugin.getTransactionLogger().log("BANK_WITHDRAW", player.getName()
                    + " | Amount: " + plugin.getEconomyManager().formatAmount(amount));
        }
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/bank transfer <player> <amount>"));
            return;
        }

        if (!player.hasPermission("myeconomy.bank.transfer")) {
            player.sendMessage(plugin.getLangManager().getMessage("general.no-permission"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getLangManager().getMessage("general.player-not-found",
                    "{player}", args[1]));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.self"));
            return;
        }

        double amount = parseAmount(player, args[2]);
        if (amount < 0) return;

        if (amount < plugin.getConfigManager().getBankMinimum()) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.minimum",
                    "{minimum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getBankMinimum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getBankMinimum())));
            return;
        }

        double bankBalance = plugin.getEconomyManager().getBankBalance(player.getUniqueId());
        if (bankBalance < amount) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.not-enough-bank",
                    "{balance}", plugin.getEconomyManager().formatBalance(bankBalance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(bankBalance)));
            return;
        }

        double taxPercent = plugin.getConfigManager().getBankTransferTaxPercent();
        double tax = amount * (taxPercent / 100.0);
        double received = amount - tax;

        plugin.getEconomyManager().bankTransfer(player.getUniqueId(), target.getUniqueId(), amount, taxPercent);

        if (tax > 0) {
            player.sendMessage(plugin.getLangManager().getMessage("bank.transfer-success-tax",
                    "{amount}", plugin.getEconomyManager().formatBalance(received),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(received),
                    "{player}", target.getName(),
                    "{tax}", plugin.getEconomyManager().formatBalance(tax)));
        } else {
            player.sendMessage(plugin.getLangManager().getMessage("bank.transfer-success",
                    "{amount}", plugin.getEconomyManager().formatBalance(amount),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(amount),
                    "{player}", target.getName()));
        }

        target.sendMessage(plugin.getLangManager().getMessage("bank.transfer-received",
                "{amount}", plugin.getEconomyManager().formatBalance(received),
                "{currency}", plugin.getEconomyManager().getCurrencyName(received),
                "{player}", player.getName()));

        plugin.getNotificationManager().showGain(target, received);

        if (plugin.getConfigManager().isLogBank()) {
            plugin.getTransactionLogger().log("BANK_TRANSFER", player.getName() + " -> " + target.getName()
                    + " | Amount: " + plugin.getEconomyManager().formatAmount(amount)
                    + (tax > 0 ? " | Tax: " + plugin.getEconomyManager().formatAmount(tax) : ""));
        }
    }

    private double parseAmount(Player player, String str) {
        try {
            double amount = Double.parseDouble(str);
            if (amount <= 0) {
                player.sendMessage(plugin.getLangManager().getMessage("pay.invalid-amount"));
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getLangManager().getMessage("pay.invalid-amount"));
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : List.of("deposit", "withdraw", "transfer")) {
                if (sub.startsWith(input)) completions.add(sub);
            }
            return completions;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("transfer")) {
                List<String> completions = new ArrayList<>();
                String input = args[1].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(input) && !p.equals(sender)) {
                        completions.add(p.getName());
                    }
                }
                return completions;
            }
            return List.of("100", "500", "1000");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("transfer")) {
            return List.of("100", "500", "1000");
        }

        return List.of();
    }
}
