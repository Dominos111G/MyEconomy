package pl.szczerbal.myeconomy.database;

import pl.szczerbal.myeconomy.MyEconomy;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final MyEconomy plugin;
    private Connection connection;

    public DatabaseManager(MyEconomy plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            String dbFile = plugin.getConfigManager().getDatabaseFile();
            File file = new File(plugin.getDataFolder(), dbFile);
            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("SQLite database connected.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    balance REAL NOT NULL DEFAULT 0.0,
                    bank_balance REAL NOT NULL DEFAULT 0.0,
                    last_interest BIGINT NOT NULL DEFAULT 0,
                    last_logout BIGINT NOT NULL DEFAULT 0
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS offline_notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    message TEXT NOT NULL,
                    timestamp BIGINT NOT NULL
                )
            """);
            migrateSchema(stmt);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }

    private void migrateSchema(Statement stmt) {
        try {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(accounts)");
            Set<String> columns = new HashSet<>();
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
            if (!columns.contains("bank_balance")) {
                stmt.executeUpdate("ALTER TABLE accounts ADD COLUMN bank_balance REAL NOT NULL DEFAULT 0.0");
            }
            if (!columns.contains("last_logout")) {
                stmt.executeUpdate("ALTER TABLE accounts ADD COLUMN last_logout BIGINT NOT NULL DEFAULT 0");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Schema migration note: " + e.getMessage());
        }
    }

    // === Account methods ===

    public boolean hasAccount(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM accounts WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
            return false;
        }
    }

    public void createAccount(UUID uuid, String name, double startingBalance) {
        double bankStarting = plugin.getConfigManager().getBankStartingBalance();
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO accounts (uuid, name, balance, bank_balance, last_interest, last_logout) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.setDouble(3, startingBalance);
            stmt.setDouble(4, bankStarting);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setLong(6, 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT balance FROM accounts WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return 0.0;
    }

    public void setBalance(UUID uuid, double balance) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE accounts SET balance = ? WHERE uuid = ?")) {
            stmt.setDouble(1, balance);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    public void updateName(UUID uuid, String name) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE accounts SET name = ? WHERE uuid = ?")) {
            stmt.setString(1, name);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    // === Bank methods ===

    public double getBankBalance(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT bank_balance FROM accounts WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("bank_balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return 0.0;
    }

    public void setBankBalance(UUID uuid, double balance) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE accounts SET bank_balance = ? WHERE uuid = ?")) {
            stmt.setDouble(1, balance);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    // === Logout time ===

    public long getLastLogout(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT last_logout FROM accounts WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("last_logout");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return 0;
    }

    public void setLastLogout(UUID uuid, long time) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE accounts SET last_logout = ? WHERE uuid = ?")) {
            stmt.setLong(1, time);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    // === Interest ===

    public long getLastInterest(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT last_interest FROM accounts WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("last_interest");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return System.currentTimeMillis();
    }

    public void setLastInterest(UUID uuid, long time) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE accounts SET last_interest = ? WHERE uuid = ?")) {
            stmt.setLong(1, time);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    // === Leaderboard ===

    public LinkedHashMap<String, Double> getTopBalances(int limit) {
        LinkedHashMap<String, Double> top = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name, balance FROM accounts ORDER BY balance DESC LIMIT ?")) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                top.put(rs.getString("name"), rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return top;
    }

    public int getRank(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) + 1 as rank FROM accounts WHERE balance > (SELECT balance FROM accounts WHERE uuid = ?)")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return -1;
    }

    public List<UUID> getAllAccounts() {
        List<UUID> accounts = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid FROM accounts")) {
            while (rs.next()) {
                accounts.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return accounts;
    }

    // === Offline notifications ===

    public void addOfflineNotification(UUID uuid, String message) {
        int max = plugin.getConfigManager().getOfflineQueueMaxMessages();
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO offline_notifications (uuid, message, timestamp) VALUES (?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, message);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
            return;
        }

        try (PreparedStatement countStmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM offline_notifications WHERE uuid = ?")) {
            countStmt.setString(1, uuid.toString());
            ResultSet rs = countStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > max) {
                try (PreparedStatement deleteStmt = connection.prepareStatement(
                        "DELETE FROM offline_notifications WHERE id IN " +
                        "(SELECT id FROM offline_notifications WHERE uuid = ? ORDER BY timestamp ASC LIMIT ?)")) {
                    deleteStmt.setString(1, uuid.toString());
                    deleteStmt.setInt(2, rs.getInt(1) - max);
                    deleteStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    public List<String> getOfflineNotifications(UUID uuid) {
        List<String> messages = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT message FROM offline_notifications WHERE uuid = ? ORDER BY timestamp ASC")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(rs.getString("message"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
        return messages;
    }

    public void clearOfflineNotifications(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM offline_notifications WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }
}
