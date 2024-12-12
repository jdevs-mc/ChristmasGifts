package dev.jdevs.JGifts.loots;

import dev.jdevs.JGifts.Christmas;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Loot {
    private final Christmas plugin;
    private final String gift;
    private final String id;
    private final ItemStack item;
    private final int chance;
    private final int maxAmount;
    private final int minAmount;
    public Loot(Christmas plugin, ItemStack item, String id, String gift, int chance, int maxAmount, int minAmount) {
        this.plugin = plugin;
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
}
