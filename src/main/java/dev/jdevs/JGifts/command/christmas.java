package dev.jdevs.JGifts.command;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.Settings;
import dev.jdevs.JGifts.events.SpawnGifts;
import dev.jdevs.JGifts.made.CustomMessages;
import dev.jdevs.JGifts.utils.ConfigManager;
import dev.jdevs.JGifts.utils.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static dev.jdevs.JGifts.Christmas.version_mode;
import static dev.jdevs.JGifts.Settings.Loots;
import static dev.jdevs.JGifts.Settings.onCrashes;
import static dev.jdevs.JGifts.events.SpawnGifts.CheckLimitGifts;
import static dev.jdevs.JGifts.events.SpawnGifts.SearchLocation;
import static dev.jdevs.JGifts.loots.Load.LoadLoots;
import static dev.jdevs.JGifts.loots.Loot.items;
import static dev.jdevs.JGifts.made.MessageLanguage.send;
import static dev.jdevs.JGifts.utils.Configurations.*;
import static dev.jdevs.JGifts.utils.Message.sendMessage;

public class christmas implements CommandExecutor {
    public FileConfiguration getConfig() {
        return Christmas.getInstance().getConfig();
    }
    List<String> no_perm = getConfig().getStringList("actions.no_perm");
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("christmas")) {
            if (Christmas.disabled) {
                sendMessage(sender, "&c&nFinish configuring of the plugin ChristmasGifts before launching it in launch.yml&r");
                return false;
            }
            else if (!sender.hasPermission("ChristmasGifts.use")) {
                for (String msg : no_perm) {
                    sendMessage(sender, msg);
                }
                return false;
            }
            Player p = null;
            if (sender instanceof Player) {
                p = (Player) sender;
            }
            if (args.length == 0 || args.length > 5) {
                send("help", p, null);
                return false;
            }
            else if (args[0].equalsIgnoreCase("newloot")) {
                if (p != null) {
                    if (args.length != 5) {
                        if (args.length >= 2) {
                            String boss = args[1];
                            if (!boss.equals("santa") && !boss.equals("grinch")) {
                                sendMessage(sender, "&cWrite santa or grinch");
                                return false;
                            }
                        }
                        sendMessage(sender, "&c/JumpChristmas newloot santa/grinch Name Amount(from-to) Chance - &fAdd an item to gifts");
                        return false;
                    }
                    ItemStack hand;
                    if (version_mode <= 8) {
                        // Ignore warnings
                        @SuppressWarnings("all")
                        ItemStack hand2 = p.getInventory().getItemInHand();
                        //
                        hand = hand2;
                    }
                    else {
                        hand = p.getInventory().getItemInMainHand();
                    }
                    if (hand.getType() == Material.AIR) {
                        sendMessage(p, "&cYou don't have an item in your hand.");
                        return false;
                    }
                    String boss = args[1];
                    String id = args[2];
                    int chance = Integer.parseInt(args[4]);
                    FileConfiguration loot = ConfigManager.of("storage\\loot.yml").getYamlConfiguration();
                    if (loot.get("loot") == null | !loot.contains("loot")) {
                        loot.createSection("loot");
                    }
                    if (loot.getConfigurationSection("loot." + boss.toLowerCase()) == null) {
                        loot.createSection("loot." + boss.toLowerCase());
                    }
                    ConfigurationSection secta = loot.getConfigurationSection("loot." + boss.toLowerCase());
                    secta.set(id + ".item", hand);
                    String[] s = args[3].split("-");
                    if (s.length == 2) {
                        secta.set(id + ".amount", args[3]);
                    }
                    else if (s.length == 1) {
                        try {
                            secta.set(id + ".amount", Integer.parseInt(args[3]));
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
                            send("error", p, "args[3]");
                            return false;
                        }
                    }
                    else {
                        send("error", p, "args[3]");
                        return false;
                    }
                    secta.set(id + ".chance", chance);

                    try {
                        loot.save(getDataFolder("storage\\loot.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Loots) {
                        items.clear();
                        LoadLoots();
                    }
                    send("successfully", p, "restart");
                } else {
                    sendMessage(sender, "&cThis command cannot be written in the console!");
                }
                return false;
            }
            else if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 1 || args.length > 3) {
                    sendMessage(sender, "&a/JumpChristmas add Player - &fPlace a gift near the player");
                    return false;
                }
                Player p1 = Bukkit.getPlayer(args[1]);
                if (CheckLimitGifts(p1) && args.length == 2 || args.length == 3) {
                    SpawnGifts.fallBlockSpawn(p1, SearchLocation(p1, 3, p1.getLocation().getYaw()));
                    send("successfully", p, null);
                }
                else {
                    for (String text : config.getStringList("actions.gift.limit")) {
                        sendMessage(p1, text);
                    }
                    send("limit", p, null);
                }
                return false;
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (args.length > 1) {
                    sendMessage(sender, "&c/JumpChristmas reload - &fReload the plugin");
                    return false;
                }
                FileConfiguration launch = ConfigManager.of("launch.yml").getYamlConfiguration();
                FileConfiguration config = ConfigManager.of("config.yml").getYamlConfiguration();
                FileConfiguration loot = ConfigManager.of("storage\\loot.yml").getYamlConfiguration();
                if (!(new File(Christmas.getInstance().getDataFolder(), "storage\\loot.yml")).exists()) {
                    Christmas.getInstance().saveResource("storage\\loot.yml", true);
                }
                if (launch.getBoolean("BaseSettings.limit_gifts")) {
                    if (!(new File(Christmas.getInstance().getDataFolder(), "storage\\db.yml")).exists()) {
                        Christmas.getInstance().saveResource("storage\\db.yml", true);
                    }
                }
                Configurations.launch = launch;
                Configurations.config = config;
                Configurations.loot = loot;
                Settings.limit = config.getBoolean("settings.gift.limit");
                Settings.debug = launch.getBoolean("BaseSettings.debug");
                Settings.max = config.getInt("settings.gift.max");
                Settings.PlaceholderAPI = launch.getBoolean("BaseSettings.supports.PlaceholderAPI");
                Settings.WorldGuard = launch.getBoolean("BaseSettings.supports.WorldGuard");
                Settings.wg = launch.getInt("BaseSettings.spawn.wg_support", 3);
                SpawnGifts.Update();
                CustomMessages.Update();
                Loots = config.getBoolean("settings.loot.enabled");
                if (Loots) {
                    items.clear();
                    LoadLoots();
                }
                send("successfully", p, null);
                return false;
            }
            else if (args[0].equalsIgnoreCase("put")) {
                if (args.length != 3) {
                    sendMessage(sender, "&c/JumpChristmas put PlayerName Amount - &fSet your gift limit for the player");
                    return false;
                }
                else {
                    String name = args[1];
                    if (Bukkit.getPlayer(name) != null) {
                        try {
                            int amount = Integer.parseInt(args[2]);
                            Configurations.nicknames.set("players." + name, amount);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            send("error", p, "args[2]");
                            return false;
                        }
                        if (onCrashes) {
                            try {
                                Configurations.nicknames.save(Configurations.getDataFolder("storage\\db.yml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        send("successfully", p, null);
                        return false;
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("check")) {
                if (args.length == 1 || args.length >= 3) {
                    sendMessage(sender, "&c/JumpChristmas check PlayerName - &fCheck player gift limit");
                    return false;
                }
                String name = args[1];
                if (Bukkit.getPlayer(name) != null) {
                    if (nicknames.get("players." + name) != null) {
                        sendMessage(p, "&fAmount: &a" + nicknames.getInt("players." + name));
                        send("successfully", p, null);
                    }
                }
                return false;
            }
            else {
                send("help", p, null);
            }
        }
        return false;
    }
}
