package pl.szczerbal.myeconomy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import pl.szczerbal.myeconomy.MyEconomy;
import pl.szczerbal.myeconomy.commands.WithdrawCommand;

public class WithdrawListener implements Listener {

    private final MyEconomy plugin;

    public WithdrawListener(MyEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(WithdrawCommand.BANKNOTE_KEY, PersistentDataType.DOUBLE)) {
            return;
        }

        event.setCancelled(true);

        double value = meta.getPersistentDataContainer().get(WithdrawCommand.BANKNOTE_KEY, PersistentDataType.DOUBLE);
        int count = item.getAmount();

        if (count > 1) {
            double totalValue = value * count;
            plugin.getEconomyManager().deposit(player.getUniqueId(), totalValue);
            item.setAmount(0);

            player.sendMessage(plugin.getLangManager().getMessage("withdraw.deposited-stack",
                    "{amount}", plugin.getEconomyManager().formatBalance(totalValue),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(totalValue),
                    "{count}", String.valueOf(count)));
        } else {
            plugin.getEconomyManager().deposit(player.getUniqueId(), value);
            item.setAmount(0);

            player.sendMessage(plugin.getLangManager().getMessage("withdraw.deposited",
                    "{amount}", plugin.getEconomyManager().formatBalance(value),
                    "{currency}", plugin.getEconomyManager().getCurrencyName(value)));
        }

        if (plugin.getConfigManager().isLogDeposit()) {
            plugin.getTransactionLogger().log("DEPOSIT", player.getName()
                    + " | Amount: " + plugin.getEconomyManager().formatAmount(value * count)
                    + " (x" + count + " banknotes)");
        }
    }
}
