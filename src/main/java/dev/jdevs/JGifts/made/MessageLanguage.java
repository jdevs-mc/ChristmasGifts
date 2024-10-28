package dev.jdevs.JGifts.made;

import dev.jdevs.JGifts.Settings;
import dev.jdevs.JGifts.utils.Message;
import dev.jdevs.JGifts.Christmas;
import org.bukkit.entity.Player;

public class MessageLanguage {
    public static void send(String type_message, Player p, String obj) {
        if (Christmas.language.contains("ru")) {
            // Russian language
            if (type_message.contains("start")) {
                type_message = "\n" +
                        "&f&l✽ &r&fРазработчик: &ahttps://vk.com/jumpstudio_mc &f&l✽\n" +
                        "\n" +
                        "&f&l✽ &aС новым годом &f&l✽\n" +
                        "\n" +
                        "&7Плагин был включён";
            } else if (type_message.contains("stop")) {
                type_message = "\n" +
                        "&f&l✽ &r&fРазработчик: &ahttps://vk.com/jumpstudio_mc &f&l✽\n" +
                        "\n" +
                        "&f&l✽ &aС новым годом &f&l✽\n" +
                        "\n" +
                        "&7Плагин был выключен";
            } else if (type_message.contains("help")) {
                type_message = "&aПомощь:\n" +
                        "\n" +
                        "&a/JumpChristmas reload - &fПерезагрузить плагин\n" +
                        "&a/JumpChristmas add Игрок - &fЗаспавнить возле игрока подарок\n" +
                        "&a/JumpChristmas put ИмяИгрока Количество - &fПоставить свой лимит подарков для игрока\n" +
                        "&a/JumpChristmas check ИмяИгрока - &fПроверьте лимит подарков для игроков\n" +
                        "&a/JumpChristmas newloot santa/grinch Название Количество(от-до) Шанс - &fДобавить предмет в подарки\n";
            } else if (type_message.contains("successfully")) {
                type_message = "&aУспешно. ";
                if (obj != null && obj.contains("restart")) {
                    type_message = type_message + "&cТребуется перезагрузка конфигурации.";
                }
            } else if (type_message.contains("limit")) {
                type_message = "&aИгрок уже забрал определённое количество подарков! :(\n" +
                        "Добавьте -f для выполнения команды.";
            } else if (type_message.contains("error")) {
                if (Settings.debug) {
                    type_message = "&c[DEBUG] Была обнаружена ошибка: ";
                    if (obj != null) {
                        type_message = type_message + obj;
                    }
                }
            }
        } else {
            // English language
            if (type_message.contains("start")) {
                type_message = "&f\n" +
                        "&f&l✽ &r&fDeveloper: &ahttps://vk.com/jumpstudio_mc &f&l✽\n" +
                        "&f\n" +
                        "&f&l✽ &aHappy New Year &f&l✽\n" +
                        "&f\n" +
                        "&7The plugin has been enabled";
            } else if (type_message.contains("stop")) {
                type_message = "\n" +
                        "&f&l✽ &r&fDeveloper: &ahttps://vk.com/jumpstudio_mc &f&l✽\n" +
                        "\n" +
                        "&f&l✽ &aHappy New Year &f&l✽\n" +
                        "\n" +
                        "&7The plugin has been disabled";
            } else if (type_message.contains("help")) {
                type_message = "&aHelp:\n" +
                        "\n" +
                        "&a/JumpChristmas reload - &fReload the plugin\n" +
                        "&a/JumpChristmas add Player - &fPlace a gift near the player\n" +
                        "&a/JumpChristmas put PlayerName Amount - &fSet your gift limit for the player\n" +
                        "&a/JumpChristmas check PlayerName - &fCheck player gift limit\n" +
                        "&a/JumpChristmas newloot santa/grinch Name Amount(from-to) Chance - &fAdd an item to gifts\n";
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
                if (!Settings.debug) {
                    return;
                }
                type_message = "&c[DEBUG] An error was found: ";
                if (obj != null) {
                    type_message = type_message + obj;
                }
            }
        }
        // Defining the type of language message
        if (p != null) {
            Message.sendMessage(p, type_message);
        } else {
            Message.sendLogger(type_message);
        }
    }
}