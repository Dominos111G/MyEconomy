package pl.szczerbal.myeconomy.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.szczerbal.myeconomy.MyEconomy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private final MyEconomy plugin;
    private FileConfiguration langConfig;
    private File langFile;
    private String prefix;

    public LangManager(MyEconomy plugin) {
        this.plugin = plugin;
        loadLang();
    }

    private void loadLang() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        prefix = langConfig.getString("prefix", "&8[&a&lMyEconomy&8] &7");
    }

    public void reload() {
        loadLang();
    }

    public String getRaw(String path) {
        String message = langConfig.getString(path, "&cMissing message: " + path);
        return message.replace("{prefix}", prefix);
    }

    public Component getMessage(String path) {
        return colorize(getRaw(path));
    }

    public Component getMessage(String path, Map<String, String> placeholders) {
        String message = getRaw(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return colorize(message);
    }

    public Component getMessage(String path, String... replacements) {
        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < replacements.length - 1; i += 2) {
            placeholders.put(replacements[i], replacements[i + 1]);
        }
        return getMessage(path, placeholders);
    }

    public static Component colorize(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public String getPrefix() {
        return prefix;
    }
}
