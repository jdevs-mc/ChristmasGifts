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
                        "&a/gifts newloot santa/grinch Название Количество(от-до) Шанс - &fДобавить предмет в подарки\n";
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
                        "&a/gifts newloot santa/grinch Name Amount(from-to) Chance - &fAdd an item to gifts\n";
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