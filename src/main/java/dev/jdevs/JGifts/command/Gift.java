package dev.jdevs.JGifts.command;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.loots.Load;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class Gift implements CommandExecutor {
    private final Christmas plugin;
    public Gift(Christmas plugin) {
        this.plugin = plugin;
    }
    private void sendMessage(CommandSender sender, String text) {
        plugin.getMessages().sendMessage(sender, text);
    }
    private void sendNoPermission(CommandSender sender) {
        plugin.getMessages().sendMessage(sender, "&c&nFinish configuring of the plugin ChristmasGifts before launching it in launch.yml&r");
    }
    private void send(String type_message, Player p, String obj) {
        plugin.getSends().send(type_message, p, obj);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 
        if (command.getName().equalsIgnoreCase("christmas")) {
            Values values = plugin.getValues();
            if (plugin.isDisabled()) {
                sendNoPermission(sender);
                return false;
            } else if (!sender.hasPermission("ChristmasGifts.use")) {
                for (String msg : values.getNo_perm()) {
                    sendMessage(sender, msg);
                }
                return false;
            }
            Player p = null;
            if (sender instanceof Player) {
                p = (Player) sender;
            }
            if (args.length == 0 || args.length > 6) {
                send("help", p, null);
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "loot": {
                    FileConfiguration loot = plugin.of("storage/loot.yml");
                    if (args.length == 1) {
                        send("loot", p, null);
                        return false;
                    }
                    switch (args[1].toLowerCase()) {
                        case "add": {
                            if (p != null) {
                                if (args.length >= 3) {
                                    String boss = args[2];
                                    if (!boss.equals("santa") && !boss.equals("grinch")) {
                                        sendMessage(sender, "&cWrite santa or grinch");
                                        return false;
                                    }
                                }
                                if (args.length != 6) {
                                    sendMessage(sender, "&c/gifts loot add santa/grinch Name Amount(from-to) Chance - &fAdd an item to gifts");
                                    return false;
                                }
                                ItemStack hand;
                                if (plugin.getVersion_mode() <= 8) {
                                    // Ignore warnings
                                    @SuppressWarnings("all")
                                    ItemStack hand2 = p.getInventory().getItemInHand();
                                    //
                                    hand = hand2;
                                } else {
                                    hand = p.getInventory().getItemInMainHand();
                                }
                                if (hand.getType() == Material.AIR) {
                                    sendMessage(p, "&cYou don't have an item in your hand.");
                                    return false;
                                }
                                String boss = args[2];
                                String id = args[3];
                                int chance = Integer.parseInt(args[5]);
                                if (loot.get("loot") == null | !loot.contains("loot")) {
                                    loot.createSection("loot");
                                }
                                if (loot.getConfigurationSection("loot." + boss.toLowerCase()) == null) {
                                    loot.createSection("loot." + boss.toLowerCase());
                                }
                                ConfigurationSection secta = loot.getConfigurationSection("loot." + boss.toLowerCase());
                                secta.set(id + ".item", hand);
                                String[] s = args[4].split("-");
                                if (s.length == 2) {
                                    secta.set(id + ".amount", args[4]);
                                } else if (s.length == 1) {
                                    try {
                                        secta.set(id + ".amount", Integer.parseInt(args[4]));
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        send("error", p, "args[3]");
                                        return false;
                                    }
                                } else {
                                    send("error", p, "args[3]");
                                    return false;
                                }
                                secta.set(id + ".chance", chance);

                                try {
                                    loot.save(new File(plugin.getDataFolder(), "storage/loot.yml"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (values.isLoots()) {
                                    plugin.getLoad().getItems().clear();
                                    plugin.getLoad().loadLoots();
                                }
                                send("successfully", p, "restart");
                            } else {
                                sendMessage(sender, "&cThis command cannot be written in the console!");
                            }
                            return false;
                        }
                        case "list": {
                            if (args.length == 3) {
                                String boss = args[2];
                                if (!boss.equals("santa") && !boss.equals("grinch")) {
                                    sendMessage(sender, "&cWrite santa or grinch");
                                    return false;
                                }
                            }
                            else {
                                sendMessage(sender, "&c/gifts loot list santa/grinch - &fGet a list of items");
                                return false;
                            }
                            String boss = args[2].toLowerCase();
                            if (loot.get("loot") == null | !loot.contains("loot")) {
                                loot.createSection("loot");
                            }
                            if (loot.getConfigurationSection("loot." + boss) == null) {
                                loot.createSection("loot." + boss);
                            }
                            Object[] list = loot.getConfigurationSection("loot." + boss).getKeys(false).toArray();
                            sendMessage(sender, "&a" + list.length + " items found (" + boss + "):\n" + Arrays.toString(list));
                            return false;
                        }
                        case "get": {
                            if (args.length >= 3) {
                                String boss = args[2];
                                if (!boss.equals("santa") && !boss.equals("grinch")) {
                                    sendMessage(sender, "&cWrite santa or grinch");
                                    return false;
                                }
                            }
                            if (args.length != 4) {
                                sendMessage(sender, "&c/gifts loot get santa/grinch Name - &fGet the item's ItemStack");
                                return false;
                            }
                            String boss = args[2].toLowerCase();
                            String id = args[3];
                            if (loot.get("loot") == null | !loot.contains("loot")) {
                                loot.createSection("loot");
                            }
                            if (loot.getConfigurationSection("loot." + boss) == null) {
                                loot.createSection("loot." + boss);
                            }
                            ConfigurationSection section = loot.getConfigurationSection("loot." + boss);
                            if (section.get(id) == null || section.get(id + ".item") == null) {
                                send("", p, null);
                                return false;
                            }
                            ItemStack stack = section.getItemStack(id + ".item");
                            sendMessage(sender, "&aItemStack " + id + " item:\n" + stack.toString() + "\nItemMeta:\n" + stack.getItemMeta().toString());
                            return false;
                        }
                        case "remove": {
                            if (args.length >= 3) {
                                String boss = args[2];
                                if (!boss.equals("santa") && !boss.equals("grinch")) {
                                    sendMessage(sender, "&cWrite santa or grinch");
                                    return false;
                                }
                            }
                            if (args.length != 4) {
                                sendMessage(sender, "&c/gifts loot remove santa/grinch Name - &fRemove an item from gifts");
                                return false;
                            }
                            String boss = args[2];
                            String id = args[3];
                            if (loot.get("loot") == null | !loot.contains("loot")) {
                                loot.createSection("loot");
                            }
                            if (loot.getConfigurationSection("loot." + boss.toLowerCase()) == null) {
                                loot.createSection("loot." + boss.toLowerCase());
                            }
                            ConfigurationSection secta = loot.getConfigurationSection("loot." + boss.toLowerCase());
                            if (secta.getConfigurationSection(id) != null) {
                                secta.set(id, null);
                            }
                            try {
                                loot.save(new File(plugin.getDataFolder(), "storage/loot.yml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (values.isLoots()) {
                                plugin.getLoad().getItems().clear();
                                plugin.getLoad().loadLoots();
                            }
                            send("successfully", p, "restart");
                            return false;
                        }
                        default: {
                            send("loot", p, null);
                            return false;
                        }
                    }
                }
                case "add": {
                    if (args.length == 1 || args.length > 3) {
                        sendMessage(sender, "&a/gifts add Player - &fPlace a gift near the player");
                        return false;
                    }
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (values.getSpawnGifts().checkLimitGifts(p1) && args.length == 2 || args.length == 3) {
                        String force;
                        if (args.length == 2) {
                            force = "";
                        }
                        else {
                            force = "_force";
                        }
                        values.getSpawnGifts().fallBlockSpawn(p1, values.getSpawnGifts().searchLocation(p1, 3, p1.getLocation().getYaw()), force);
                        send("successfully", p, null);
                    } else {
                        for (String text : values.getLimit_message()) {
                            sendMessage(p1, text);
                        }
                        send("limit", p, null);
                    }
                    return false;
                }
                case "reload": {
                    if (args.length > 1) {
                        sendMessage(sender, "&c/gifts reload - &fReload the plugin");
                        return false;
                    }
                    Values val = plugin.getValues();
                    Load load = plugin.getLoad();
                    plugin.createConfigurations();
                    if (val.isPlaceholderAPI()) {
                        val.getPlaceholderAPI().unregister();
                        val.connectPlaceholderAPI();
                    }
                    val.setupValues(false);
                    if (val.isLoots()) {
                        load.getItems().clear();
                        load.loadLoots();
                    }
                    send("successfully", p, null);
                    return false;
                }
                case "put": {
                    if (args.length != 3) {
                        sendMessage(sender, "&c/gifts put PlayerName Amount - &fSet your gift limit for the player");
                        return false;
                    } else {
                        String name = args[1];
                        YamlConfiguration nicknames = plugin.getNicknames();
                        if (Bukkit.getPlayer(name) != null) {
                            try {
                                int amount = Integer.parseInt(args[2]);
                                nicknames.set("players." + name, amount);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                send("error", p, "args[2]");
                                return false;
                            }
                            if (plugin.getValues().isOnCrashes()) {
                                try {
                                    nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            send("successfully", p, null);
                            return false;
                        }
                    }
                    return false;
                }
                case "check": {
                    if (args.length == 1 || args.length >= 3) {
                        sendMessage(sender, "&c/gifts check PlayerName - &fCheck player gift limit");
                        return false;
                    }
                    String name = args[1];
                    YamlConfiguration nicknames = plugin.getNicknames();
                    if (Bukkit.getPlayer(name) != null) {
                        if (nicknames.get("players." + name) != null) {
                            sendMessage(sender, "&fAmount: &a" + nicknames.getInt("players." + name));
                            send("successfully", p, null);
                        }
                    }
                    return false;
                }
                default: {
                    send("help", p, null);
                    return false;
                }
            }
        }
        return false;
    }
    // Previously, this command was duplicated on GitHub, but not in the plugin.
}
