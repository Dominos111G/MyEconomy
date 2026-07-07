package pl.szczerbal.myeconomy.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import pl.szczerbal.myeconomy.MyEconomy;
import pl.szczerbal.myeconomy.config.LangManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WithdrawCommand implements CommandExecutor, TabCompleter {

    private final MyEconomy plugin;
    public static final NamespacedKey BANKNOTE_KEY = new NamespacedKey("myeconomy", "banknote_value");

    public WithdrawCommand(MyEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("withdraw").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getMessage("general.player-only"));
            return true;
        }

        if (!plugin.getConfigManager().isWithdrawEnabled()) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.disabled"));
            return true;
        }

        if (!player.hasPermission("myeconomy.withdraw")) {
            player.sendMessage(plugin.getLangManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getLangManager().getMessage("general.usage",
                    "{usage}", "/withdraw <amount>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.invalid-amount"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.invalid-amount"));
            return true;
        }

        if (amount < plugin.getConfigManager().getWithdrawMinimum()) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.minimum",
                    "{minimum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getWithdrawMinimum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getWithdrawMinimum())));
            return true;
        }

        if (amount > plugin.getConfigManager().getWithdrawMaximum()) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.maximum",
                    "{maximum}", plugin.getEconomyManager().formatBalance(plugin.getConfigManager().getWithdrawMaximum()),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(plugin.getConfigManager().getWithdrawMaximum())));
            return true;
        }

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        if (balance < amount) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.not-enough",
                    "{balance}", plugin.getEconomyManager().formatBalance(balance),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(balance)));
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getLangManager().getMessage("withdraw.inventory-full"));
            return true;
        }

        plugin.getEconomyManager().withdraw(player.getUniqueId(), amount);

        ItemStack banknote = createBanknote(player, amount);
        player.getInventory().addItem(banknote);

        player.sendMessage(plugin.getLangManager().getMessage("withdraw.success",
                "{amount}", plugin.getEconomyManager().formatBalance(amount),
                "{currency}", plugin.getEconomyManager().getCurrencyName(amount)));

        if (plugin.getConfigManager().isLogWithdraw()) {
            plugin.getTransactionLogger().log("WITHDRAW", player.getName()
                    + " | Amount: " + plugin.getEconomyManager().formatAmount(amount));
        }

        return true;
    }

    public ItemStack createBanknote(Player player, double amount) {
        ItemStack item = resolveItem();
        ItemMeta meta = item.getItemMeta();

        String formattedAmount = plugin.getEconomyManager().formatBalance(amount);
        String currency = plugin.getEconomyManager().getCurrencyName(amount);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String name = plugin.getConfigManager().getWithdrawItemName()
                .replace("{amount}", formattedAmount)
                .replace("{currency}", currency)
                .replace("{player}", player.getName())
                .replace("{date}", date);
        meta.displayName(LangManager.colorize(name));

        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getConfigManager().getWithdrawItemLore()) {
            lore.add(LangManager.colorize(line
                    .replace("{amount}", formattedAmount)
                    .replace("{currency}", currency)
                    .replace("{player}", player.getName())
                    .replace("{date}", date)));
        }
        meta.lore(lore);

        int customModelData = plugin.getConfigManager().getWithdrawCustomModelData();
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        meta.getPersistentDataContainer().set(BANKNOTE_KEY, PersistentDataType.DOUBLE, amount);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack resolveItem() {
        String materialStr = plugin.getConfigManager().getWithdrawItemMaterial();

        if (materialStr.contains(":")) {
            ItemStack customItem = resolveCustomPluginItem(materialStr);
            if (customItem != null) return customItem;
            plugin.getLogger().warning("Could not resolve custom item '" + materialStr + "', falling back to PAPER.");
            return new ItemStack(Material.PAPER);
        }

        Material material = Material.matchMaterial(materialStr);
        if (material != null) {
            return new ItemStack(material);
        }

        plugin.getLogger().warning("Unknown material '" + materialStr + "', falling back to PAPER.");
        return new ItemStack(Material.PAPER);
    }

    private ItemStack resolveCustomPluginItem(String id) {
        String[] parts = id.split(":", 2);
        String pluginName = parts[0].toLowerCase();
        String itemId = parts[1];

        try {
            switch (pluginName) {
                case "oraxen" -> {
                    Class<?> oraxenItems = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
                    var method = oraxenItems.getMethod("getItemById", String.class);
                    var builder = method.invoke(null, itemId);
                    if (builder != null) {
                        var buildMethod = builder.getClass().getMethod("build");
                        return (ItemStack) buildMethod.invoke(builder);
                    }
                }
                case "itemsadder" -> {
                    Class<?> customStack = Class.forName("dev.lone.itemsadder.api.CustomStack");
                    var method = customStack.getMethod("getInstance", String.class);
                    var instance = method.invoke(null, id);
                    if (instance != null) {
                        var getItem = instance.getClass().getMethod("getItemStack");
                        return (ItemStack) getItem.invoke(instance);
                    }
                }
                case "nexo" -> {
                    Class<?> nexoItems = Class.forName("com.nexomc.nexo.api.NexoItems");
                    var method = nexoItems.getMethod("itemFromId", String.class);
                    var builder = method.invoke(null, itemId);
                    if (builder != null) {
                        var buildMethod = builder.getClass().getMethod("build");
                        return (ItemStack) buildMethod.invoke(builder);
                    }
                }
                default -> {
                    Material mat = Material.matchMaterial(id);
                    if (mat != null) return new ItemStack(mat);
                }
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Plugin '" + pluginName + "' not found for custom item: " + id);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resolve custom item '" + id + "': " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return List.of("100", "500", "1000", "5000");
        }
        return List.of();
    }
}
