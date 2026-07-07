package pl.szczerbal.myeconomy.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import pl.szczerbal.myeconomy.MyEconomy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final MyEconomy plugin;
    private final Map<Integer, CachedTopEntry> topCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 30_000;

    public PlaceholderAPIHook(MyEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "myeconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Dominos111G";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String lower = params.toLowerCase();

        // %myeconomy_top_name_1% - name of player at rank 1
        if (lower.startsWith("top_name_")) {
            int rank = parseRank(lower.substring("top_name_".length()));
            if (rank < 1) return "";
            CachedTopEntry entry = getTopEntry(rank);
            return entry != null ? entry.name : "---";
        }

        // %myeconomy_top_balance_1% - formatted balance of player at rank 1
        if (lower.startsWith("top_balance_")) {
            int rank = parseRank(lower.substring("top_balance_".length()));
            if (rank < 1) return "";
            CachedTopEntry entry = getTopEntry(rank);
            return entry != null ? plugin.getEconomyManager().formatBalance(entry.balance) : "$0.00";
        }

        // %myeconomy_top_balance_raw_1% - raw balance of player at rank 1
        if (lower.startsWith("top_balance_raw_")) {
            int rank = parseRank(lower.substring("top_balance_raw_".length()));
            if (rank < 1) return "";
            CachedTopEntry entry = getTopEntry(rank);
            return entry != null ? String.valueOf(entry.balance) : "0";
        }

        if (player == null) return "";

        return switch (lower) {
            case "balance" -> plugin.getEconomyManager().formatBalance(
                    plugin.getEconomyManager().getBalance(player.getUniqueId()));
            case "balance_raw" -> String.valueOf(
                    plugin.getEconomyManager().getBalance(player.getUniqueId()));
            case "balance_formatted" -> plugin.getEconomyManager().formatAmount(
                    plugin.getEconomyManager().getBalance(player.getUniqueId()));
            case "rank" -> String.valueOf(
                    plugin.getEconomyManager().getRank(player.getUniqueId()));
            case "currency" -> plugin.getEconomyManager().getCurrencyName(
                    plugin.getEconomyManager().getBalance(player.getUniqueId()));
            case "currency_symbol" -> plugin.getConfigManager().getCurrencySymbol();
            case "bank_balance" -> plugin.getEconomyManager().formatBalance(
                    plugin.getEconomyManager().getBankBalance(player.getUniqueId()));
            case "bank_balance_raw" -> String.valueOf(
                    plugin.getEconomyManager().getBankBalance(player.getUniqueId()));
            case "total_balance" -> plugin.getEconomyManager().formatBalance(
                    plugin.getEconomyManager().getBalance(player.getUniqueId())
                    + plugin.getEconomyManager().getBankBalance(player.getUniqueId()));
            default -> null;
        };
    }

    private int parseRank(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private CachedTopEntry getTopEntry(int rank) {
        refreshCacheIfNeeded();
        return topCache.get(rank);
    }

    private void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate < CACHE_TTL) return;

        lastCacheUpdate = now;
        topCache.clear();

        int maxEntries = plugin.getConfigManager().getLeaderboardEntries();
        LinkedHashMap<String, Double> top = plugin.getEconomyManager().getTopBalances(maxEntries);

        int rank = 1;
        for (Map.Entry<String, Double> entry : top.entrySet()) {
            topCache.put(rank, new CachedTopEntry(entry.getKey(), entry.getValue()));
            rank++;
        }
    }

    private record CachedTopEntry(String name, double balance) {}
}
