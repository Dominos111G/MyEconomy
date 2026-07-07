package pl.szczerbal.myeconomy.economy;

import org.bukkit.OfflinePlayer;
import pl.szczerbal.myeconomy.MyEconomy;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.UUID;

public class EconomyManager {

    private final MyEconomy plugin;
    private DecimalFormat formatter;

    public EconomyManager(MyEconomy plugin) {
        this.plugin = plugin;
        this.formatter = new DecimalFormat(plugin.getConfigManager().getCurrencyFormat());
    }

    public boolean hasAccount(UUID uuid) {
        return plugin.getDatabaseManager().hasAccount(uuid);
    }

    public void createAccount(UUID uuid, String name) {
        if (!hasAccount(uuid)) {
            plugin.getDatabaseManager().createAccount(uuid, name, plugin.getConfigManager().getStartingBalance());
        } else {
            plugin.getDatabaseManager().updateName(uuid, name);
        }
    }

    public double getBalance(UUID uuid) {
        return plugin.getDatabaseManager().getBalance(uuid);
    }

    public void setBalance(UUID uuid, double amount) {
        if (!plugin.getConfigManager().isAllowNegativeBalance()) {
            amount = Math.max(0, amount);
        }
        plugin.getDatabaseManager().setBalance(uuid, amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        double balance = getBalance(uuid);
        if (!plugin.getConfigManager().isAllowNegativeBalance() && balance < amount) {
            return false;
        }
        setBalance(uuid, balance - amount);
        return true;
    }

    public void deposit(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean has(UUID uuid, double amount) {
        if (plugin.getConfigManager().isAllowNegativeBalance()) return true;
        return getBalance(uuid) >= amount;
    }

    public LinkedHashMap<String, Double> getTopBalances(int limit) {
        return plugin.getDatabaseManager().getTopBalances(limit);
    }

    public int getRank(UUID uuid) {
        return plugin.getDatabaseManager().getRank(uuid);
    }

    // === Bank methods ===

    public double getBankBalance(UUID uuid) {
        return plugin.getDatabaseManager().getBankBalance(uuid);
    }

    public void setBankBalance(UUID uuid, double amount) {
        plugin.getDatabaseManager().setBankBalance(uuid, Math.max(0, amount));
    }

    public boolean bankDeposit(UUID uuid, double amount) {
        if (!withdraw(uuid, amount)) return false;
        double maxBank = plugin.getConfigManager().getBankMaxBalance();
        double currentBank = getBankBalance(uuid);
        if (maxBank > 0 && currentBank + amount > maxBank) {
            deposit(uuid, amount);
            return false;
        }
        setBankBalance(uuid, currentBank + amount);
        return true;
    }

    public boolean bankWithdraw(UUID uuid, double amount) {
        double bankBalance = getBankBalance(uuid);
        if (bankBalance < amount) return false;
        setBankBalance(uuid, bankBalance - amount);
        deposit(uuid, amount);
        return true;
    }

    public boolean bankTransfer(UUID from, UUID to, double amount, double taxPercent) {
        double bankBalance = getBankBalance(from);
        if (bankBalance < amount) return false;
        double tax = amount * (taxPercent / 100.0);
        double received = amount - tax;
        setBankBalance(from, bankBalance - amount);
        setBankBalance(to, getBankBalance(to) + received);
        return true;
    }

    // === Formatting ===

    public String formatBalance(double amount) {
        return plugin.getConfigManager().getCurrencySymbol() + formatter.format(amount);
    }

    public String formatAmount(double amount) {
        return formatter.format(amount);
    }

    public String getCurrencyName(double amount) {
        return amount == 1.0
                ? plugin.getConfigManager().getCurrencySingular()
                : plugin.getConfigManager().getCurrencyPlural();
    }

    public void refreshFormatter() {
        this.formatter = new DecimalFormat(plugin.getConfigManager().getCurrencyFormat());
    }
}
