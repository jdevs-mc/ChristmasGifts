package dev.jdevs.JGifts.loots;

import dev.jdevs.JGifts.Settings;
import dev.jdevs.JGifts.utils.Configurations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static dev.jdevs.JGifts.made.MessageLanguage.send;

public class Loot {
    public static List<Loot> items = new ArrayList<>();
    private final String gift;
    private final String id;
    private final ItemStack item;
    private final int chance;
    private final int maxAmount;
    private final int minAmount;
    public Loot(ItemStack item, String id, String gift, int chance, int maxAmount, int minAmount) {
        this.id = id;
        this.gift = gift;
        this.item = item;
        this.chance = chance;
        this.maxAmount = maxAmount;
        this.minAmount = minAmount;
    }
    public ItemStack parseItem() {
        item.setAmount(getRandAmount());
        return item;
    }
    public String getGift() {
        return this.gift;
    }
    public String getId() {
        return this.id;
    }
    public int getChance() {
        return this.chance;
    }
    public int getRandAmount() {
        try {
            if ((maxAmount - minAmount) < 0) {
                return maxAmount;
            }
            return minAmount + ThreadLocalRandom.current().nextInt(maxAmount - minAmount);
        } catch (Throwable ignored) {
            return 1;
        }
    }
    public static void dropLoot(Location loc) {
        String type;
        if (!Configurations.config.getBoolean("settings.grinch.enabled")) {
            type = "santa";
        } else {
            int currentChance2 = ThreadLocalRandom.current().nextInt(101);
            if (Configurations.config.getInt("settings.grinch.chance") >= currentChance2) {
                type = "grinch";
            } else {
                type = "santa";
            }
        }
        if (Settings.Loots) {
            for (Loot lt : Loot.items) {
                if (lt.getGift().contains(type)) {
                    int currentChance = ThreadLocalRandom.current().nextInt(101);
                    if (lt.getChance() >= currentChance) {
                        try {
                            ItemStack itemStack = lt.parseItem();
                            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                                loc.getWorld().dropItem(loc, itemStack);
                            }
                        } catch (Throwable e) {
                            send("error", null, "The item is defective. Fix it in loot.yml. Item-id: " + type + "." + lt.getId());
                        }
                    }
                }
            }
        }
    }
}
