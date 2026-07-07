# 💰 MyEconomy

**A feature-rich economy plugin for Paper Minecraft 1.20.x–1.21.x servers**

MyEconomy gives your server a complete economy system out of the box — wallet, bank accounts, physical banknotes, passive income, PvP money stealing, and full Vault integration. Every feature is toggleable, every message is customizable, and other plugins can hook into it seamlessly.

## ✨ Key Features

### 💵 Core Economy
- Wallet balance with configurable starting amount and currency
- Optional negative balance support
- SQLite database — no external setup required
- Full **Vault** provider — other plugins read and write balances through the standard API

### 🏦 Bank System
- Separate savings account per player (`/bank`)
- Deposit from wallet, withdraw to wallet, transfer to other players' banks
- Configurable max balance and transfer tax
- Can be disabled entirely in config

### 📄 Physical Banknotes (Withdraw)
- `/withdraw` creates a real item — right-click to deposit back
- Stack of banknotes? Right-click deposits the entire stack at once
- Configurable item material — vanilla (`PAPER`, `GOLD_INGOT`) or custom plugins:
  - **Oraxen** → `oraxen:banknote`
  - **ItemsAdder** → `itemsadder:custom_paper`
  - **Nexo** → `nexo:money_note`
- Custom model data support for resource packs
- Fully customizable name and lore with `{amount}`, `{player}`, `{date}` placeholders
- Can be disabled entirely in config

### 📈 Passive Income (3 Systems)
- **Interest** — percentage of wallet balance earned at configurable intervals (online only)
- **Playtime Rewards** — multi-tier system with per-permission rates (VIP+, VIP, default, or any custom tiers you define)
- **Offline Growth** — compound interest calculated on login based on time away (configurable interval, max hours, growth cap)

### ⚔️ PvP Money
- Killer steals a configurable percentage of victim's balance on kill
- Min/max steal limits per kill
- Option for victim to not lose money (killer gets it from thin air)
- Both players see ActionBar feedback instantly

### 🔔 Smart Notifications
- **ActionBar popups** — every gain/loss shows `+$1.23` or `-$20.00` on the action bar
- **Offline queue** — transfers, bank deposits, and events while offline are stored in the database and shown on login
- Configurable duration, formats, and max queued messages

### 💳 Payments with Tax
- `/pay` supports optional tax percentage
- Configurable min/max payment amounts
- Self-pay prevention (toggleable)

### 🏆 Leaderboard
- `/balancetop` shows top richest players with formatted display
- Your own entry is highlighted
- Your personal rank shown at the bottom
- Configurable entry count and cache interval

### 🧩 Integrations
- **Vault** — full economy provider with deposit/withdraw/getBalance/has/createAccount
- **PlaceholderAPI** — 12+ placeholders including leaderboard positions
- **Transaction logging** — every action logged to file with timestamps

### ♻️ Live Reload & Translations
- `/myeconomy reload` applies changes without a restart
- Every message lives in `lang.yml` — fully translatable
- Color codes and hex colors supported

## 📥 Installation

1. Download the latest `.jar` from [Releases](../../releases)
2. Place it in your server's `plugins/` folder
3. Restart the server
4. Edit `plugins/MyEconomy/config.yml` and `plugins/MyEconomy/lang.yml`
5. Run `/myeconomy reload`

**Requirements:** Paper (or fork) 1.20.x–1.21.x, Java 21.

**Optional dependencies:**
- [Vault](https://www.spigotmc.org/resources/vault.34315/) — allows other plugins to interact with MyEconomy
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — adds placeholders for scoreboards, holograms, tab, etc.

## 🎮 Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/balance [player]` | Check wallet balance | `myeconomy.balance` / `.others` |
| `/balancetop` | Top richest players | `myeconomy.balancetop` |
| `/pay <player> <amount>` | Send money to a player | `myeconomy.pay` |
| `/withdraw <amount>` | Create a physical banknote | `myeconomy.withdraw` |
| `/bank` | View bank balance | `myeconomy.bank` |
| `/bank deposit <amount>` | Wallet → Bank | `myeconomy.bank` |
| `/bank withdraw <amount>` | Bank → Wallet | `myeconomy.bank` |
| `/bank transfer <player> <amount>` | Your bank → Player's bank | `myeconomy.bank.transfer` |

**Aliases:** `/bal` for `/balance`, `/baltop` for `/balancetop`

## 🔧 Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/myeconomy set <player> <amount>` | Set player's wallet balance | `myeconomy.admin` |
| `/myeconomy change <player> <value>` | Add or remove from balance | `myeconomy.admin` |
| `/myeconomy reload` | Reload config and lang files | `myeconomy.admin` |

The `change` command accepts positive and negative values (e.g. `/myeconomy change Steve -500`).

## 🔐 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `myeconomy.balance` | `true` | Check own balance |
| `myeconomy.balance.others` | `true` | Check other players' balance |
| `myeconomy.balancetop` | `true` | View leaderboard |
| `myeconomy.pay` | `true` | Pay other players |
| `myeconomy.withdraw` | `true` | Create banknotes |
| `myeconomy.bank` | `true` | Use bank system |
| `myeconomy.bank.transfer` | `true` | Transfer between bank accounts |
| `myeconomy.playtime.vip` | — | VIP playtime reward tier |
| `myeconomy.playtime.vipplus` | — | VIP+ playtime reward tier |
| `myeconomy.admin` | `op` | Admin commands (set, change, reload) |

### Example: LuckPerms

```
/lp group vip permission set myeconomy.playtime.vip true
/lp group vip-plus permission set myeconomy.playtime.vipplus true
/lp group admin permission set myeconomy.admin true
```

## 🧩 PlaceholderAPI

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

### Player Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%myeconomy_balance%` | Formatted wallet balance (e.g. `$1,234.56`) |
| `%myeconomy_balance_raw%` | Raw wallet balance number (e.g. `1234.56`) |
| `%myeconomy_balance_formatted%` | Balance without currency symbol (e.g. `1,234.56`) |
| `%myeconomy_bank_balance%` | Formatted bank balance |
| `%myeconomy_bank_balance_raw%` | Raw bank balance number |
| `%myeconomy_total_balance%` | Wallet + Bank combined |
| `%myeconomy_rank%` | Player's wealth rank |
| `%myeconomy_currency%` | Currency name (singular/plural) |
| `%myeconomy_currency_symbol%` | Currency symbol (e.g. `$`) |

### Leaderboard Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%myeconomy_top_name_<N>%` | Name of player at rank N |
| `%myeconomy_top_balance_<N>%` | Formatted balance at rank N |
| `%myeconomy_top_balance_raw_<N>%` | Raw balance at rank N |

Replace `<N>` with any number (e.g. `%myeconomy_top_name_1%` for the richest player). Cached with 30-second TTL.

## ⚙️ Configuration

MyEconomy uses focused config files:

| File | Purpose |
|------|---------|
| `config.yml` | All plugin settings (economy, bank, interest, PvP, etc.) |
| `lang.yml` | Every player-facing message (translatable) |
| `economy.db` | SQLite database (auto-generated) |
| `transactions.log` | Transaction history (auto-generated) |

### 💵 Currency Settings

```yaml
starting-balance: 100.0
allow-negative-balance: false

currency:
  name-singular: "Dollar"
  name-plural: "Dollars"
  symbol: "$"
  format: "#,##0.00"
```

### 🏦 Bank System

```yaml
bank:
  enabled: true
  starting-balance: 0.0
  minimum: 1.0
  max-balance: 0.0             # 0 = unlimited
  transfer-tax-percent: 0.0    # tax on bank-to-bank transfers
```

### 📈 Interest (Online)

```yaml
interest:
  enabled: true
  rate-percent: 0.5
  interval-minutes: 60
  max-balance-for-interest: 100000.0
  min-balance-for-interest: 100.0
```

### 🌙 Offline Growth

Compound interest calculated when a player logs in, based on how long they were offline.

```yaml
offline-growth:
  enabled: true
  interval-minutes: 60      # growth applied per this interval
  rate-percent: 0.3          # percent per interval
  max-hours: 12              # max offline time counted
  max-growth: 5000.0         # cap per session (0 = no cap)
  min-balance: 50.0          # minimum balance to qualify
```

A player offline for 6 hours with $10,000 and 0.3% per hour earns compound growth over 6 intervals.

### 🎮 Playtime Reward Tiers

Tiers are checked top-to-bottom. Each player receives **one** reward — the best tier they have permission for.

```yaml
playtime-rewards:
  enabled: true
  interval-minutes: 15
  tiers:
    vip-plus:
      permission: "myeconomy.playtime.vipplus"
      type: "percent"        # "percent" or "flat"
      amount: 2.0
      min-balance: 0.0
      max-reward: 2000.0
    vip:
      permission: "myeconomy.playtime.vip"
      type: "percent"
      amount: 1.0
      min-balance: 0.0
      max-reward: 1000.0
    default:
      permission: "default"  # applies to everyone
      type: "percent"
      amount: 0.5
      min-balance: 0.0
      max-reward: 500.0
```

Add as many tiers as you need — just give each a unique key and permission node.

### ⚔️ PvP Money

```yaml
pvp-money:
  enabled: true
  steal-percent: 5.0
  minimum: 1.0          # victim must have at least this much
  maximum: 1000.0       # max stolen per kill (0 = no cap)
  victim-loses: true     # false = killer gets money from thin air
```

### 📄 Banknote Item

Supports vanilla materials and custom items from other plugins:

```yaml
withdraw:
  enabled: true
  minimum: 1.0
  maximum: 1000000.0
  item-material: "PAPER"                    # vanilla material
  # item-material: "oraxen:banknote"        # Oraxen
  # item-material: "itemsadder:custom_paper" # ItemsAdder
  # item-material: "nexo:money_note"        # Nexo
  custom-model-data: 0                      # for resource packs
  item-name: "&a&lBanknote &7- &f{amount}"
  item-lore:
    - ""
    - "&7Value: &a{amount} {currency}"
    - "&7Created by: &f{player}"
    - "&7Date: &f{date}"
    - ""
    - "&e&lRight-click &7to deposit"
```

### 🔔 Notifications

```yaml
notifications:
  actionbar:
    enabled: true
    duration-ticks: 60                # 60 ticks = 3 seconds
    gain-format: "&a+{amount}"
    loss-format: "&c-{amount}"
  offline-queue:
    enabled: true
    max-messages: 20
```

### 💳 Pay Settings

```yaml
pay:
  minimum: 1.0
  maximum: 1000000.0
  tax-percent: 0.0       # tax on /pay transfers
  allow-self-pay: false
```

### 📝 Transaction Logging

```yaml
logging:
  enabled: true
  file: "transactions.log"
  log-pay: true
  log-withdraw: true
  log-deposit: true
  log-admin-changes: true
  log-interest: false
  log-playtime-rewards: false
  log-bank: true
  log-pvp: true
  log-offline-growth: true
```

Log format: `[2026-07-07 15:30:45] [PAY] Steve -> Alex | Amount: 500.00`

## 📊 Use Cases

### Basic Server Economy

```yaml
# config.yml — just wallet + pay, everything else off
starting-balance: 500.0
bank:
  enabled: false
withdraw:
  enabled: false
interest:
  enabled: false
offline-growth:
  enabled: false
playtime-rewards:
  enabled: false
pvp-money:
  enabled: false
```

### Survival with PvP Risk

```yaml
pvp-money:
  enabled: true
  steal-percent: 10.0
  maximum: 5000.0
  victim-loses: true

bank:
  enabled: true          # players can protect money in the bank
```

Players keep money safe in the bank — only wallet balance is at risk in PvP.

### VIP Rewards Server

```yaml
playtime-rewards:
  enabled: true
  interval-minutes: 10
  tiers:
    mvp:
      permission: "myeconomy.playtime.mvp"
      type: "flat"
      amount: 50.0
      max-reward: 0.0
    vip:
      permission: "myeconomy.playtime.vip"
      type: "flat"
      amount: 25.0
      max-reward: 0.0
    default:
      permission: "default"
      type: "flat"
      amount: 10.0
      max-reward: 0.0

offline-growth:
  enabled: true
  rate-percent: 0.5
  max-hours: 8
```

MVPs earn $50 every 10 minutes, VIPs earn $25, everyone else $10. All players earn offline growth up to 8 hours.

## 🔌 Vault Integration

MyEconomy registers as a full Vault economy provider. Other plugins can:

```java
// Get MyEconomy through Vault — standard API
Economy economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

economy.getBalance(player);              // read balance
economy.depositPlayer(player, 100.0);    // add money
economy.withdrawPlayer(player, 50.0);    // remove money
economy.has(player, 200.0);              // check if player can afford
economy.format(1234.56);                 // "$1,234.56"
```

Set `vault-integration: false` in config to disable — MyEconomy will still work standalone with its SQLite database.

## 🐛 Troubleshooting

### Plugin won't load

**Check:**
1. Are you using Paper 1.20.x–1.21.x? (`/version`)
2. Is the JAR in the `plugins/` folder?
3. Do you have Java 21? (`java -version`)

### Vault says no economy found

**Check:**
1. Is Vault installed and loaded? (Check console for `[Vault]`)
2. Is `vault-integration: true` in config?
3. MyEconomy must load **after** Vault — it's handled automatically via `softdepend`

### Balance not saving

**Check:**
1. The `economy.db` file exists in `plugins/MyEconomy/`
2. The server has write permissions to that folder
3. Check console for SQLite errors

### Playtime rewards not working

**Check:**
1. `playtime-rewards.enabled: true`
2. Tiers are defined under `playtime-rewards.tiers`
3. Player has the correct permission for their tier (or tier uses `permission: "default"`)
4. `interval-minutes` has passed since server start

### Banknote right-click does nothing

**Check:**
1. The item has the MyEconomy NBT tag — only banknotes created by `/withdraw` work
2. Renaming or modifying the item in an anvil doesn't remove the tag
3. Custom item material is valid and the plugin (Oraxen/ItemsAdder) is loaded

### Offline growth not applying

**Check:**
1. `offline-growth.enabled: true`
2. Player's balance is above `min-balance`
3. Player was offline for at least one full `interval-minutes` period
4. Growth is calculated on **login**, not periodically

## 📈 Performance

MyEconomy is lightweight and built for clean performance:

- ✅ SQLite database — no external server required
- ✅ Offline growth calculated on login, not with background tasks
- ✅ PlaceholderAPI leaderboard cached with configurable TTL
- ✅ Async-safe notification queue
- ✅ No external dependencies (Vault and PlaceholderAPI are optional soft-depends)
- ✅ Single JAR, zero configuration required to start

## 🔁 Version History

### Future Roadmap
- [ ] MySQL / MariaDB support
- [ ] In-game GUI for bank management
- [ ] Shop system integration
- [ ] Economy statistics and graphs
- [ ] Per-world economy separation

**1.0.0**
- Initial release: wallet, bank accounts, banknotes (with Oraxen/ItemsAdder/Nexo support), interest, offline growth, multi-tier playtime rewards, PvP money stealing, ActionBar notifications, offline notification queue, Vault provider, PlaceholderAPI (12+ placeholders with leaderboard), transaction logging, admin commands, tab completion, full `lang.yml` translation support.

## 🔗 Links

- **GitHub:** https://github.com/Dominos111G/MyEconomy
- **Modrinth:** https://modrinth.com/project/myeconomy
- **Issues:** https://github.com/Dominos111G/MyEconomy/issues

## 📝 License

MIT License - Free to modify and distribute

## 📖 Documentation

- [config.yml](src/main/resources/config.yml) — All plugin settings
- [lang.yml](src/main/resources/lang.yml) — All translatable messages
- [plugin.yml](src/main/resources/plugin.yml) — Commands and permissions

## 💬 Support

Found a bug or want a feature? [Open an issue](https://github.com/Dominos111G/MyEconomy/issues)!

---

**Version:** 1.0.0
**Minecraft:** 1.20.x–1.21.x
**Made with ❤️ for Minecraft servers**
