package dev.jdevs.JGifts.made;

import dev.jdevs.JGifts.Christmas;
import org.bukkit.entity.Player;

public final class MessageLanguage {
    private final Christmas plugin;
    private final boolean debug;
    public MessageLanguage(Christmas plugin) {
        this.plugin = plugin;
        debug = plugin.getValues().isDebug();
    }
    public void send(String type_message, Player p, String obj) {
        if (Christmas.language.contains("ru")) {
            // Russian language
            if (type_message.contains("start")) {
                type_message = "\n" +
                        "&f&l✽ &r&fРазработчик: &ahttps://vk.com/jdevs &f&l✽\n" +
                        "\n" +
                        "&f&l✽ &aС новым годом &f&l✽\n" +
                        "\n" +
                        "&7Плагин был включён.";
            } else if (type_message.contains("stop")) {
                type_message = "\n" +
                        "&f&l✽ &r&fРазработчик: &ahttps://vk.com/jdevs &f&l✽\n" +
                        "\n" +
                        "&f&l✽ &aС новым годом &f&l✽\n" +
                        "\n" +
                        "&7Плагин был выключен (v1.2.0)";
            } else if (type_message.contains("help")) {
                type_message = "&aПомощь:\n" +
                        "\n" +
                        "&a/gifts reload - &fПерезагрузить плагин\n" +
                        "&a/gifts add Игрок - &fЗаспавнить возле игрока подарок\n" +
                        "&a/gifts put ИмяИгрока Количество - &fПоставить свой лимит подарков для игрока\n" +
                        "&a/gifts check ИмяИгрока - &fПроверьте лимит подарков для игроков\n" +
                        "&a/gifts loot - &fПолучить помощь о подкоманде loot";
            } else if (type_message.contains("loot")) {
                type_message = "&aПомощь для подкоманды loot:\n" +
                        "\n" +
                        "&a/gifts loot add santa/grinch Название Количество(от-до) Шанс - &fДобавьте предмет в подарки\n" +
                        "&a/gifts loot list santa/grinch - &fПолучите названия предметов у подарков\n" +
                        "&a/gifts loot get santa/grinch Название - &fПолучите характеристику (ItemStack) предмета\n" +
                        "&a/gifts loot remove santa/grinch Название - &fУдалить предмет из подарков";
            } else if (type_message.contains("successfully")) {
                type_message = "&aУспешно. ";
                if (obj != null && obj.contains("restart")) {
                    type_message = type_message + "&cТребуется перезагрузка конфигурации.";
                }
            } else if (type_message.contains("limit")) {
                type_message = "&aИгрок уже забрал определённое количество подарков! :(\n" +
                        "Добавьте -f для выполнения команды.";
            } else if (type_message.contains("error")) {
                if (!debug) {
                    return;
                }
                type_message = "&c[DEBUG] Была обнаружена ошибка: ";
                if (obj != null) {
                    type_message = type_message + obj;
                }
            }
            else {
                type_message = "Здесь пусто.";
            }
        } else {
            // English language
            if (type_message.contains("start")) {
                type_message = "&f\n" +
                        "&f&l✽ &r&fDeveloper: &ahttps://vk.com/jdevs &f&l✽\n" +
                        "&f\n" +
                        "&f&l✽ &aHappy New Year &f&l✽\n" +
                        "&f\n" +
                        "&7The plugin has been enabled.";
            } else if (type_message.contains("stop")) {
                type_message = "\n" +
                        "&f&l✽ &r&fDeveloper: &ahttps://vk.com/jdevs &f&l✽\n" +
                        "\n" +
                        "&f&l✽ &aHappy New Year &f&l✽\n" +
                        "\n" +
                        "&7The plugin has been disabled (v1.2.0)";
            } else if (type_message.contains("help")) {
                type_message = "&aHelp:\n" +
                        "\n" +
                        "&a/gifts reload - &fReload the plugin\n" +
                        "&a/gifts add Player - &fPlace a gift near the player\n" +
                        "&a/gifts put PlayerName Amount - &fSet your gift limit for the player\n" +
                        "&a/gifts check PlayerName - &fCheck player gift limit\n" +
                        "&a/gifts loot - &fGet help about the loot subcommand";
            } else if (type_message.contains("loot")) {
                type_message = "&aHelp from the loot subcommand:\n" +
                        "\n" +
                        "&a/gifts loot add santa/grinch Name Amount(from-to) Chance - &fAdd an item to gifts\n" +
                        "&a/gifts loot list santa/grinch - &fGet a list of items\n" +
                        "&a/gifts loot get santa/grinch Name - &fGet the item's ItemStack\n" +
                        "&a/gifts loot remove santa/grinch Name - &fRemove an item from gifts";
            } else if (type_message.contains("successfully")) {
                type_message = "&aSuccessfully. ";
                if (obj != null && obj.contains("restart")) {
                    type_message = type_message + "A configuration reboot is required.";
                }
            } else if (type_message.contains("limit")) {
                type_message = "&aThe player has already collected a certain number of gifts! :(\n" +
                        "Add -f to the command to execute.";
            }
            else if (type_message.contains("error")) {
                if (!debug) {
                    return;
                }
                type_message = "&c[DEBUG] An error was found: ";
                if (obj != null) {
                    type_message = type_message + obj;
                }
            }
            else {
                type_message = "Null";
            }
        }
        // Defining the type of language message
        if (p != null) {
            plugin.getMessages().sendMessage(p, type_message, null);
        } else {
            plugin.getMessages().sendLogger(type_message);
        }
    }
}