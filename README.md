# 🎁 **ChristmasGifts**
## 🛷 Deliver gifts to your server that will bring pleasant surprises to your players 
This plugin adds a gift delivery mechanic for your players.
# 🚶 Wander the world
Wandering around the world, the player can get a gift from Santa, but did the Grinch put his rags in it?

![2024_10_29_15_47_36](https://github.com/user-attachments/assets/9dbf7a66-2234-45f5-85d9-f3538e6392ef)
## 🛠️ Supported:
### 🔮 Support HEX (1.16+)
### 🌍 Languages:
- EN (English)
- RU (Russian)
### 🌐 Plugins:
- **WorldGuard, WorldEdit** - Check where your gifts will spawn.
- **PlaceholderAPI** - Add the ability to interact with the plugin via placeholders, and also add placeholders to messages from the configuration.
- **DecentHolograms** - Put a hologram over the gift. 

![image](https://github.com/user-attachments/assets/50eaaa9b-1cfe-4609-b305-85d4453c44eb)

### 🔎 Checks:
- The plugin will automatically detect your server version so that it starts working correctly with your project.
- The plugin will check the working conditions and, if necessary, will warn about errors and turn off.
- Replacement of some parts of the configuration in case of their absence.
- The gift was protected from destruction attempts.

![2024_10_29_16_23_061-ezgif com-optimize](https://github.com/user-attachments/assets/143b3c1d-d69b-4a13-bd5e-10acf4750c32)

## ♾️ Functions:
### ⌨️Command (/gifts):
Get a list of commands using /gifts help
- /gifts reload - Reload the plugin
- /gifts add Player - Spawn a gift near the player
- /gifts put NamePlayer Amount - Set your gift limit for the player
- /gifts check NamePlayer - Check the gift limit for players
- /gifts newloot santa/grinch Name Amount(from-to) Chance - Add an item to gifts
### 🚀Launch:
- Functions for saving limits and gifts when crashing on your server.
- Auto-issue a gift to make the issue faster.
- Select the operating mode. This plugin can work in the following operating modes:
  On command - 0
  Chance when the player moves - 1
  Every some time (minutes, hours) - 2
// Use mode 2 for a better experience with plug-in performance
- Chance adjustment.
- The lock is spawned in the world, the biome.
- The lock is spawned with fly and shift.
### ⚙️Config:
- Send actions to players using messages. (HEX support from 1.16+)
- Settings for gifts, loot, holograms
### 🎁Loot:
- Loot checks and in case of errors, errors will be sent to the console.
- Add loot via the /gifts command
### 🔐Storage:
- Storing limit data and gifts in case of crashes using a file.
## 📝 Configurations:
View them by navigating through the files using the following path: src\main\resources

# You can consider the rest of the possibilities when using the plugin.
