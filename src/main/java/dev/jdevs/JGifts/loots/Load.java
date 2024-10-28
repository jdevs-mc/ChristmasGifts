package dev.jdevs.JGifts.loots;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import static dev.jdevs.JGifts.Settings.Loots;
import static dev.jdevs.JGifts.loots.Loot.items;
import static dev.jdevs.JGifts.made.MessageLanguage.send;
import static dev.jdevs.JGifts.utils.Configurations.loot;

public class Load {
    public static void LoadLoots() {
        if (!Loots) {
            return;
        }
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
                    send("error", null, "The amount of the item is incorrect. Fix it in loot.yml. Item-id: " + type + "." + i);
                    amountMax = 1;
                    amountMin = 1;
                }
                try {
                    ItemStack stack = new ItemStack(section.getItemStack(i + ".item"));
                    items.add(new Loot(stack, i, type, section.getInt(i + ".chance"), amountMax, amountMin));
                } catch (Exception e) {
                    send("error", null, "The item is defective. Fix it in loot.yml. Item-id: " + i);
                }
            }
        }
    }
}
