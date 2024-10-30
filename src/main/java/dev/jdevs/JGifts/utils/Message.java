package dev.jdevs.JGifts.utils;

import dev.jdevs.JGifts.Settings;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.jdevs.JGifts.made.MessageLanguage.send;
import static org.bukkit.Bukkit.getServer;

public class Message {
    public static String hex(String message) {
        String[] ver = getServer().getVersion().split("\\.");
        if (Integer.parseInt(ver[1]) < 16) {
            Pattern pattern = Pattern.compile("(#[a-fA-F0-9]{6})");
            for (Matcher matcher = pattern.matcher(message); matcher.find(); matcher = pattern.matcher(message)) {
                String hexCode = message.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');
                char[] ch = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder();
                for (char c : ch) {
                    builder.append("&").append(c);
                }
                message = message.replace(hexCode, builder.toString());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message).replace('&', '§');
    }
    public static void sendMessage(Player p, String text) {
        if (text == null) {
            return;
        }
        String formatted = text.
                replace("%player%", p.getName()).
                replace("%p", p.getName());
        if (formatted.contains("%rnd_player%")) {
            List<Player> playersList = new ArrayList<>(Bukkit.getOnlinePlayers());
            String name = playersList.get(ThreadLocalRandom.current().nextInt(playersList.size())).getName();
            formatted = formatted.replace("%rnd_player%", name);
        }
        String lowerCase = formatted.toLowerCase();
        if (Settings.PlaceholderAPI) {
            formatted = PlaceholderAPI.setPlaceholders(p, formatted);
        }
        if (!lowerCase.startsWith("[") || lowerCase.startsWith("[message] ")) {
            p.sendMessage(hex(formatted.replace("[message] ", "")));
        }
        else if (lowerCase.startsWith("[player] ")) {
            p.performCommand(hex(formatted.replace("[player] ", "")));
        }
        else if (lowerCase.startsWith("[console] ")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hex(formatted.replace("[console] ", "")));
        }
        else if (lowerCase.startsWith("[broadcast] ")) {
            for (Player pls : Bukkit.getOnlinePlayers()) {
                pls.sendMessage(hex(formatted.replace("[broadcast] ", "")));
            }
        }
        else if (lowerCase.startsWith("[sound] ")) {
            playSound(p, formatted);
        }
        else {
            p.sendMessage(hex(text));
        }
    }
    public static void sendMessage(CommandSender sender, String text) {
        String lowerCase = text.toLowerCase();
        if (!lowerCase.startsWith("[") || lowerCase.startsWith("[message] ")) {
            sender.sendMessage(hex(text.replace("[message] ", "")));
        }
        else {
            sender.sendMessage(hex(text));
        }
    }
    public static void sendLogger(String text) {
        Bukkit.getConsoleSender().sendMessage(hex(text));
    }
    public static void playSound(Player p, String formatted) {
        formatted = formatted.replace("[sound] ", "");
        Sound sound;
        int volume = 1;
        int pitch = 1;
        if (formatted.contains(";")) {
            String[] split = formatted.split(";");
            sound = Sound.valueOf(split[0]);
            volume = Integer.parseInt(split[1]);
            pitch = Integer.parseInt(split[2]);
        }
        else {
            sound = Sound.valueOf(formatted);
        }
        try {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
        catch (Exception e) {
            send("error", null, "An error occurred when calling sound from the configuration");
            e.printStackTrace();
        }
    }
}
