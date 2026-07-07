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

public class MyEconomyCommand implements CommandExecutor, TabCompleter {

    private final MyEconomy plugin;

    public MyEconomyCommand(MyEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("myeconomy").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("myeconomy.admin")) {
            sender.sendMessage(plugin.getLangManager().getMessage("admin.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> handleSet(sender, args);
            case "change" -> handleChange(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/myeconomy set <player> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.player-not-found",
                    "{player}", args[1]));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLangManager().getMessage("pay.invalid-amount"));
            return;
        }

        if (amount < 0) amount = 0;

        plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
        sender.sendMessage(plugin.getLangManager().getMessage("admin.set",
                "{player}", target.getName(),
                "{amount}", plugin.getEconomyManager().formatBalance(amount),
                "{currency}", plugin.getEconomyManager().getCurrencyName(amount)));

        if (plugin.getConfigManager().isLogAdminChanges()) {
            plugin.getTransactionLogger().log("ADMIN_SET", sender.getName() + " set "
                    + target.getName() + "'s balance to " + plugin.getEconomyManager().formatAmount(amount));
        }
    }

    private void handleChange(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/myeconomy change <player> <value>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.player-not-found",
                    "{player}", args[1]));
            return;
        }

        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLangManager().getMessage("pay.invalid-amount"));
            return;
        }

        if (value > 0) {
            plugin.getEconomyManager().deposit(target.getUniqueId(), value);
        } else if (value < 0) {
            plugin.getEconomyManager().withdraw(target.getUniqueId(), Math.abs(value));
        }

        double newBalance = plugin.getEconomyManager().getBalance(target.getUniqueId());
        String langKey = value >= 0 ? "admin.change-add" : "admin.change-remove";

        sender.sendMessage(plugin.getLangManager().getMessage(langKey,
                "{player}", target.getName(),
                "{amount}", plugin.getEconomyManager().formatBalance(Math.abs(value)),
                "{currency}", plugin.getEconomyManager().getCurrencyName(Math.abs(value)),
                "{new_balance}", plugin.getEconomyManager().formatBalance(newBalance)));

        if (plugin.getConfigManager().isLogAdminChanges()) {
            plugin.getTransactionLogger().log("ADMIN_CHANGE", sender.getName() + " changed "
                    + target.getName() + "'s balance by " + plugin.getEconomyManager().formatAmount(value)
                    + " | New: " + plugin.getEconomyManager().formatAmount(newBalance));
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        plugin.getEconomyManager().refreshFormatter();
        sender.sendMessage(plugin.getLangManager().getMessage("admin.reload"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(plugin.getLangManager().getMessage("general.usage",
                "{usage}", "/myeconomy <set|change|reload>"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!sender.hasPermission("myeconomy.admin")) return List.of();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : List.of("set", "change", "reload")) {
                if (sub.startsWith(input)) completions.add(sub);
            }
            return completions;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("change"))) {
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) return List.of("100", "1000", "10000");
            if (args[0].equalsIgnoreCase("change")) return List.of("-1000", "-100", "100", "1000");
        }

        return List.of();
    }
}
