package pl.szczerbal.myeconomy.hooks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import pl.szczerbal.myeconomy.MyEconomy;

import java.util.Collections;
import java.util.List;

public class VaultEconomyProvider implements Economy {

    private final MyEconomy plugin;

    public VaultEconomyProvider(MyEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "MyEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return plugin.getEconomyManager().formatBalance(amount);
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getConfigManager().getCurrencyPlural();
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfigManager().getCurrencySingular();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return plugin.getEconomyManager().hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return false;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.getEconomyManager().getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return 0;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return plugin.getEconomyManager().has(player.getUniqueId(), amount);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }
        if (!has(player, amount)) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        plugin.getEconomyManager().withdraw(player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use UUID-based methods");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use UUID-based methods");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }
        plugin.getEconomyManager().deposit(player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use UUID-based methods");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use UUID-based methods");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        String name = player.getName() != null ? player.getName() : "Unknown";
        plugin.getEconomyManager().createAccount(player.getUniqueId(), name);
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }

    // Bank methods - not supported
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return notImplemented(); }
    @Override public EconomyResponse createBank(String name, String player) { return notImplemented(); }
    @Override public EconomyResponse deleteBank(String name) { return notImplemented(); }
    @Override public EconomyResponse bankBalance(String name) { return notImplemented(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notImplemented(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notImplemented(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notImplemented(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return notImplemented(); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return notImplemented(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return notImplemented(); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return notImplemented(); }
    @Override public List<String> getBanks() { return Collections.emptyList(); }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
}
