package pl.szczerbal.myeconomy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.szczerbal.myeconomy.commands.*;
import pl.szczerbal.myeconomy.config.ConfigManager;
import pl.szczerbal.myeconomy.config.LangManager;
import pl.szczerbal.myeconomy.database.DatabaseManager;
import pl.szczerbal.myeconomy.economy.EconomyManager;
import pl.szczerbal.myeconomy.hooks.PlaceholderAPIHook;
import pl.szczerbal.myeconomy.hooks.VaultHook;
import pl.szczerbal.myeconomy.listeners.PlayerJoinListener;
import pl.szczerbal.myeconomy.listeners.PvPListener;
import pl.szczerbal.myeconomy.listeners.WithdrawListener;
import pl.szczerbal.myeconomy.util.*;

public class MyEconomy extends JavaPlugin {

    private static MyEconomy instance;
    private ConfigManager configManager;
    private LangManager langManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private TransactionLogger transactionLogger;
    private MoneyNotificationManager notificationManager;
    private OfflineGrowthCalculator offlineGrowthCalculator;
    private VaultHook vaultHook;
    private InterestTask interestTask;
    private PlaytimeRewardTask playtimeRewardTask;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        langManager = new LangManager(this);
        transactionLogger = new TransactionLogger(this);
        databaseManager = new DatabaseManager(this);
        economyManager = new EconomyManager(this);
        notificationManager = new MoneyNotificationManager(this);
        offlineGrowthCalculator = new OfflineGrowthCalculator(this);

        if (configManager.isVaultIntegration() && Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(this);
            vaultHook.register();
            getLogger().info("Vault integration enabled.");
        } else {
            if (configManager.isVaultIntegration()) {
                getLogger().warning("Vault not found! Using internal database.");
            }
            getLogger().info("Using internal SQLite database for economy.");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && configManager.isPlaceholderAPI()) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI integration enabled.");
        }

        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("balancetop").setExecutor(new BalanceTopCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("withdraw").setExecutor(new WithdrawCommand(this));
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("myeconomy").setExecutor(new MyEconomyCommand(this));

        getServer().getPluginManager().registerEvents(new WithdrawListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);

        if (configManager.isInterestEnabled()) {
            interestTask = new InterestTask(this);
            interestTask.start();
            getLogger().info("Interest system enabled (" + configManager.getInterestRate() + "% every " + configManager.getInterestInterval() + " minutes).");
        }

        if (configManager.isPlaytimeRewardsEnabled()) {
            playtimeRewardTask = new PlaytimeRewardTask(this);
            playtimeRewardTask.start();
            int tierCount = configManager.getPlaytimeRewardTiers().size();
            getLogger().info("Playtime rewards enabled (" + tierCount + " tier(s), every " + configManager.getPlaytimeRewardsInterval() + " minutes).");
        }

        if (configManager.isBankEnabled()) {
            getLogger().info("Bank system enabled.");
        }

        if (configManager.isOfflineGrowthEnabled()) {
            getLogger().info("Offline growth enabled (" + configManager.getOfflineGrowthRate() + "% every " + configManager.getOfflineGrowthInterval() + " min, max " + configManager.getOfflineGrowthMaxHours() + "h).");
        }

        if (configManager.isPvpMoneyEnabled()) {
            getLogger().info("PvP money enabled (" + configManager.getPvpStealPercent() + "% steal on kill).");
        }

        getLogger().info("MyEconomy v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (interestTask != null) {
            interestTask.stop();
        }
        if (playtimeRewardTask != null) {
            playtimeRewardTask.stop();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("MyEconomy disabled.");
    }

    public void reload() {
        configManager.reload();
        langManager.reload();

        if (interestTask != null) {
            interestTask.stop();
            interestTask = null;
        }
        if (configManager.isInterestEnabled()) {
            interestTask = new InterestTask(this);
            interestTask.start();
        }

        if (playtimeRewardTask != null) {
            playtimeRewardTask.stop();
            playtimeRewardTask = null;
        }
        if (configManager.isPlaytimeRewardsEnabled()) {
            playtimeRewardTask = new PlaytimeRewardTask(this);
            playtimeRewardTask.start();
        }
    }

    public static MyEconomy getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public TransactionLogger getTransactionLogger() {
        return transactionLogger;
    }

    public MoneyNotificationManager getNotificationManager() {
        return notificationManager;
    }

    public OfflineGrowthCalculator getOfflineGrowthCalculator() {
        return offlineGrowthCalculator;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
