package dev.jdevs.JGifts.loots;

import dev.jdevs.JGifts.Christmas;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class Load {
    private final List<Loot> items = new ArrayList<>();
    Christmas plugin;
    public Load(Christmas plugin) {
        this.plugin = plugin;
    }
    public void loadLoots() {
        if (!plugin.getValues().isLoots()) {
            return;
        }
        plugin.updateLoot();
        YamlConfiguration loot = plugin.getLoot();
        if (loot.get("loot") == null | !loot.contains("loot")) {
            loot.createSection("loot");
        }
        for (String type : loot.getConfigurationSection("loot").getKeys(false)) {
            if (!loot.contains("loot." + type.toLowerCase())) {
                loot.createSection("loot." + type.toLowerCase());
            }
            ConfigurationSection section = loot.getConfigurationSection("loot." + type);
            if (section == null) {
                return;
            }
            for (String i : section.getKeys(false)) {
                int amountMax;
                int amountMin;
                try {
                    Object o = section.get(i + ".amount");
                    if (o instanceof Integer) {
                        Integer b = (Integer) o;
                        amountMax = b;
                        amountMin = b;
                    } else {
                        String[] args = ((String) o).split("-");
                        amountMax = Integer.parseInt(args[1]);
                        amountMin = Integer.parseInt(args[0]);
                        if (amountMax < amountMin) {
                            throw new RuntimeException();
                        }
                    }
                } catch (Throwable e) {
                    plugin.getSends().send("error", null, "The amount of the item is incorrect. Fix it in loot.yml. Item-id: " + type + "." + i);
                    amountMax = 1;
                    amountMin = 1;
                }
                try {
                    ItemStack stack = new ItemStack(section.getItemStack(i + ".item"));
                    items.add(new Loot(plugin, stack, i, type, section.getInt(i + ".chance"), amountMax, amountMin));
                } catch (Exception e) {
                    plugin.getSends().send("error", null, "The item is defective. Fix it in loot.yml. Item-id: " + i);
                }
            }
        }
    }
    public void dropLoot(Location loc) {
        String type;
        if (!plugin.getValues().isGrinch()) {
            type = "santa";
        } else {
            int currentChance2 = ThreadLocalRandom.current().nextInt(101);
            if (plugin.getValues().getChance() >= currentChance2) {
                type = "grinch";
            } else {
                type = "santa";
            }
        }
        if (plugin.getValues().isLoots()) {
            for (Loot lt : items) {
                if (lt.getGift().contains(type)) {
                    int currentChance = ThreadLocalRandom.current().nextInt(101);
                    if (lt.getChance() >= currentChance) {
                        try {
                            ItemStack itemStack = lt.parseItem();
                            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                                loc.getWorld().dropItem(loc, itemStack);
                            }
                        } catch (Throwable e) {
                            plugin.getSends().send("error", null, "The item is defective. Fix it in loot.yml. Item-id: " + type + "." + lt.getId());
                        }
                    }
                }
            }
        }
    }
}
