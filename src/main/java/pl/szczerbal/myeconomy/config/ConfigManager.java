package pl.szczerbal.myeconomy.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.szczerbal.myeconomy.MyEconomy;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final MyEconomy plugin;
    private FileConfiguration config;

    public ConfigManager(MyEconomy plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public double getStartingBalance() {
        return config.getDouble("starting-balance", 100.0);
    }

    public boolean isAllowNegativeBalance() {
        return config.getBoolean("allow-negative-balance", false);
    }

    public String getCurrencySymbol() {
        return config.getString("currency.symbol", "$");
    }

    public String getCurrencySingular() {
        return config.getString("currency.name-singular", "Dollar");
    }

    public String getCurrencyPlural() {
        return config.getString("currency.name-plural", "Dollars");
    }

    public String getCurrencyFormat() {
        return config.getString("currency.format", "#,##0.00");
    }

    public boolean isVaultIntegration() {
        return config.getBoolean("vault-integration", true);
    }

    public String getDatabaseFile() {
        return config.getString("database.file", "economy.db");
    }

    public double getPayMinimum() {
        return config.getDouble("pay.minimum", 1.0);
    }

    public double getPayMaximum() {
        return config.getDouble("pay.maximum", 1000000.0);
    }

    public double getPayTaxPercent() {
        return config.getDouble("pay.tax-percent", 0.0);
    }

    public boolean isAllowSelfPay() {
        return config.getBoolean("pay.allow-self-pay", false);
    }

    public boolean isWithdrawEnabled() {
        return config.getBoolean("withdraw.enabled", true);
    }

    public double getWithdrawMinimum() {
        return config.getDouble("withdraw.minimum", 1.0);
    }

    public double getWithdrawMaximum() {
        return config.getDouble("withdraw.maximum", 1000000.0);
    }

    public String getWithdrawItemMaterial() {
        return config.getString("withdraw.item-material", "PAPER");
    }

    public int getWithdrawCustomModelData() {
        return config.getInt("withdraw.custom-model-data", 0);
    }

    public String getWithdrawItemName() {
        return config.getString("withdraw.item-name", "&a&lBanknote &7- &f{amount}");
    }

    public List<String> getWithdrawItemLore() {
        return config.getStringList("withdraw.item-lore");
    }

    public boolean isInterestEnabled() {
        return config.getBoolean("interest.enabled", true);
    }

    public double getInterestRate() {
        return config.getDouble("interest.rate-percent", 0.5);
    }

    public int getInterestInterval() {
        return config.getInt("interest.interval-minutes", 60);
    }

    public double getInterestMaxBalance() {
        return config.getDouble("interest.max-balance-for-interest", 100000.0);
    }

    public double getInterestMinBalance() {
        return config.getDouble("interest.min-balance-for-interest", 100.0);
    }

    public boolean isPlaytimeRewardsEnabled() {
        return config.getBoolean("playtime-rewards.enabled", true);
    }

    public int getPlaytimeRewardsInterval() {
        return config.getInt("playtime-rewards.interval-minutes", 15);
    }

    public List<PlaytimeRewardTier> getPlaytimeRewardTiers() {
        List<PlaytimeRewardTier> tiers = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("playtime-rewards.tiers");
        if (section == null) return tiers;

        for (String key : section.getKeys(false)) {
            ConfigurationSection tierSection = section.getConfigurationSection(key);
            if (tierSection == null) continue;

            tiers.add(new PlaytimeRewardTier(
                    key,
                    tierSection.getString("permission", "default"),
                    tierSection.getString("type", "percent"),
                    tierSection.getDouble("amount", 0.5),
                    tierSection.getDouble("min-balance", 0.0),
                    tierSection.getDouble("max-reward", 500.0)
            ));
        }
        return tiers;
    }

    public record PlaytimeRewardTier(String name, String permission, String type,
                                     double amount, double minBalance, double maxReward) {
        public boolean isDefault() {
            return "default".equalsIgnoreCase(permission);
        }
    }

    // Bank
    public boolean isBankEnabled() {
        return config.getBoolean("bank.enabled", true);
    }

    public double getBankStartingBalance() {
        return config.getDouble("bank.starting-balance", 0.0);
    }

    public double getBankMinimum() {
        return config.getDouble("bank.minimum", 1.0);
    }

    public double getBankMaxBalance() {
        return config.getDouble("bank.max-balance", 0.0);
    }

    public double getBankTransferTaxPercent() {
        return config.getDouble("bank.transfer-tax-percent", 0.0);
    }

    // Offline growth
    public boolean isOfflineGrowthEnabled() {
        return config.getBoolean("offline-growth.enabled", true);
    }

    public int getOfflineGrowthInterval() {
        return config.getInt("offline-growth.interval-minutes", 60);
    }

    public double getOfflineGrowthRate() {
        return config.getDouble("offline-growth.rate-percent", 0.3);
    }

    public int getOfflineGrowthMaxHours() {
        return config.getInt("offline-growth.max-hours", 12);
    }

    public double getOfflineGrowthMaxGrowth() {
        return config.getDouble("offline-growth.max-growth", 5000.0);
    }

    public double getOfflineGrowthMinBalance() {
        return config.getDouble("offline-growth.min-balance", 50.0);
    }

    // PvP money
    public boolean isPvpMoneyEnabled() {
        return config.getBoolean("pvp-money.enabled", true);
    }

    public double getPvpStealPercent() {
        return config.getDouble("pvp-money.steal-percent", 5.0);
    }

    public double getPvpMinimum() {
        return config.getDouble("pvp-money.minimum", 1.0);
    }

    public double getPvpMaximum() {
        return config.getDouble("pvp-money.maximum", 1000.0);
    }

    public boolean isPvpVictimLoses() {
        return config.getBoolean("pvp-money.victim-loses", true);
    }

    // Notifications
    public boolean isActionbarEnabled() {
        return config.getBoolean("notifications.actionbar.enabled", true);
    }

    public int getActionbarDuration() {
        return config.getInt("notifications.actionbar.duration-ticks", 60);
    }

    public String getActionbarGainFormat() {
        return config.getString("notifications.actionbar.gain-format", "&a+{amount}");
    }

    public String getActionbarLossFormat() {
        return config.getString("notifications.actionbar.loss-format", "&c-{amount}");
    }

    public boolean isOfflineQueueEnabled() {
        return config.getBoolean("notifications.offline-queue.enabled", true);
    }

    public int getOfflineQueueMaxMessages() {
        return config.getInt("notifications.offline-queue.max-messages", 20);
    }

    // Logging extras
    public boolean isLogBank() {
        return config.getBoolean("logging.log-bank", true);
    }

    public boolean isLogPvp() {
        return config.getBoolean("logging.log-pvp", true);
    }

    public boolean isLogOfflineGrowth() {
        return config.getBoolean("logging.log-offline-growth", true);
    }

    public boolean isLoggingEnabled() {
        return config.getBoolean("logging.enabled", true);
    }

    public String getLogFile() {
        return config.getString("logging.file", "transactions.log");
    }

    public boolean isLogPay() {
        return config.getBoolean("logging.log-pay", true);
    }

    public boolean isLogWithdraw() {
        return config.getBoolean("logging.log-withdraw", true);
    }

    public boolean isLogDeposit() {
        return config.getBoolean("logging.log-deposit", true);
    }

    public boolean isLogAdminChanges() {
        return config.getBoolean("logging.log-admin-changes", true);
    }

    public boolean isLogInterest() {
        return config.getBoolean("logging.log-interest", false);
    }

    public boolean isLogPlaytimeRewards() {
        return config.getBoolean("logging.log-playtime-rewards", false);
    }

    public boolean isPlaceholderAPI() {
        return config.getBoolean("placeholderapi", true);
    }

    public int getLeaderboardEntries() {
        return config.getInt("leaderboard.entries", 10);
    }

    public int getLeaderboardUpdateInterval() {
        return config.getInt("leaderboard.update-interval-seconds", 300);
    }
}
