package dev.jdevs.JGifts.utils;

import dev.jdevs.JGifts.Christmas;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Message {
    private final Christmas plugin;
    private final int version_mode;
    private final boolean isPlaceholderAPI;
    public Message(Christmas plugin) {
        this.plugin = plugin;
        version_mode = plugin.getVersion_mode();
        if (!plugin.isDisabled()) {
            isPlaceholderAPI = plugin.getValues().isPlaceholderAPI();
        }
        else {
            isPlaceholderAPI = false;
        }
    }
    public String hex(String message) {
        if (version_mode >= 16) {
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
    public void sendMessage(Player p, String text, Location loc) {
        if (text == null) {
            return;
        }
        String formatted = text.
                replace("%player%", p.getName());
        if (formatted.contains("%rnd_player%")) {
            List<Player> playersList = new ArrayList<>(Bukkit.getOnlinePlayers());
            String name = playersList.get(ThreadLocalRandom.current().nextInt(playersList.size())).getName();
            formatted = formatted.replace("%rnd_player%", name);
        }
        String lowerCase = formatted.toLowerCase();
        if (isPlaceholderAPI) {
            formatted = PlaceholderAPI.setPlaceholders(p, formatted);
        }
        if (!lowerCase.startsWith("[") || lowerCase.startsWith("[message] ")) {
            text = hex(formatted.replace("[message] ", ""));
            p.sendMessage(text);
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
        else if (lowerCase.startsWith("[particle] ")) {
            if (loc == null) {
                return;
            }
            formatted = formatted.replace("[particle] ", "");
            String[] split = formatted.split(";");
            String type = split[0];
            int amount = Integer.parseInt(split[1]);
            String[] rgb = split[2].split("\\.");
            int r = Integer.parseInt(rgb[0]);
            int g = Integer.parseInt(rgb[1]);
            int b = Integer.parseInt(rgb[2]);
            if (split.length == 3) {
                sendParticle(loc.clone(), type, amount, r, g, b, 0);
            }
            else if (split.length == 4) {
                double radius = Double.parseDouble(split[3]);
                sendParticle(loc.clone(), type, amount, r, g, b, radius);
            }
        }
        else {
            p.sendMessage(hex(text));
        }
    }
    public void sendMessage(CommandSender sender, String text) {
        String lowerCase = text.toLowerCase();
        if (!lowerCase.startsWith("[") || lowerCase.startsWith("[message] ")) {
            sender.sendMessage(hex(text.replace("[message] ", "")));
        }
        else {
            sender.sendMessage(hex(text));
        }
    }
    public void sendLogger(String text) {
        Bukkit.getConsoleSender().sendMessage(hex(text));
    }
    private void sendParticle(Location loc, String type, int amount, int r, int g, int b, double radius) {
        type = type.toLowerCase();
        if (type.contains("one")) {
            loc.add(0.5, 0.5, 0.5);
            if (version_mode >= 13) {
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(r, g, b), 1);
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), amount, 0, 0, 0, dust);
            }
            else if (version_mode >= 9) {
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, (double) (r + 1) / 255, (double) (g + 1) / 255, (double) (b + 1) / 255, 1);
            }
            else {
                sendLogger("&c[ChristmasGifts] Supports particles starting &lonly&r&c from version &l1.9.1+");
            }
        } else {
            loc.add(0.5, 0.1, 0.5);
            for (double t = 0; t <= 4*Math.PI*radius; t += 0.1) {
                double x = radius * Math.cos(t) + loc.getX();
                double z = loc.getZ() + radius * Math.sin(t);
                if (version_mode >= 13) {
                    Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(r, g, b), 1);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, x, loc.getY(), z, amount, 0, 0, 0, dust);
                }
                else if (version_mode >= 9) {
                    loc.getWorld().spawnParticle(Particle.REDSTONE, x, loc.getY(), z, 0, (double) (r + 1) / 255, (double) (g + 1) / 255, (double) (b + 1) / 255, 1);
                }
                else {
                    sendLogger("&c[ChristmasGifts] Supports particles starting &lonly&r&c from version &l1.9.1+");
                }
            }
        }
    }
    private void playSound(Player p, String formatted) {
        formatted = formatted.replace("[sound] ", "");
        Sound sound;
        int volume = 1;
        int pitch = 1;
        try {
            if (formatted.contains(";")) {
                String[] split = formatted.split(";");
                sound = Sound.valueOf(split[0]);
                volume = Integer.parseInt(split[1]);
                pitch = Integer.parseInt(split[2]);
            }
            else {
                sound = Sound.valueOf(formatted);
            }
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
        catch (Exception e) {
            plugin.getSends().send("error", null, "An error occurred when calling sound from the configuration");
            e.printStackTrace();
        }
    }
}
