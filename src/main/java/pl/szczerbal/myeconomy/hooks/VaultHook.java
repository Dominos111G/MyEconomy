package pl.szczerbal.myeconomy.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import pl.szczerbal.myeconomy.MyEconomy;

public class VaultHook {

    private final MyEconomy plugin;
    private VaultEconomyProvider provider;

    public VaultHook(MyEconomy plugin) {
        this.plugin = plugin;
    }

    public void register() {
        provider = new VaultEconomyProvider(plugin);
        Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Normal);
        plugin.getLogger().info("Registered Vault economy provider.");
    }

    public void unregister() {
        if (provider != null) {
            Bukkit.getServicesManager().unregister(Economy.class, provider);
        }
    }
}
