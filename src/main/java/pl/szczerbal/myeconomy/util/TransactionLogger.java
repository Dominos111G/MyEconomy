package pl.szczerbal.myeconomy.util;

import pl.szczerbal.myeconomy.MyEconomy;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionLogger {

    private final MyEconomy plugin;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionLogger(MyEconomy plugin) {
        this.plugin = plugin;
    }

    public void log(String type, String message) {
        if (!plugin.getConfigManager().isLoggingEnabled()) return;

        String logFile = plugin.getConfigManager().getLogFile();
        File file = new File(plugin.getDataFolder(), logFile);

        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(writer)) {
                String timestamp = LocalDateTime.now().format(FORMATTER);
                bw.write("[" + timestamp + "] [" + type + "] " + message);
                bw.newLine();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write transaction log: " + e.getMessage());
        }
    }
}
