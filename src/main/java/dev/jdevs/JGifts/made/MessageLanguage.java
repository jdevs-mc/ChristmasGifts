package dev.jdevs.JGifts.made;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Message;
import org.bukkit.entity.Player;

public final class MessageLanguage {
    private final Message messages;
    private final boolean debug;
    private final String language;
    public MessageLanguage(Christmas plugin) {
        debug = plugin.getValues().isDebug();
        messages = plugin.getMessages();
        language = plugin.getLanguage();
    }
    public void send(String type_message, Player p, String obj) {
        if (language.equalsIgnoreCase("ru")) {
            // Russian language
            type_message = getRU(type_message, obj);
        } else {
            // English language
            type_message = getEN(type_message, obj);
        }
        // Defining the type of language message
        if (type_message != null && !type_message.equals("null")) {
            if (p != null) {
                messages.sendMessage(p, type_message, null);
            } else {
                messages.sendLogger(type_message);
            }
        }
    }
    private String getEN(String message, String obj) {
        if (message.equalsIgnoreCase("start")) {
            message = "&f\n" +
                    "&f&l✽ &r&fDeveloper: &ahttps://vk.com/jdevs &f&l✽\n" +
                    "&f\n" +
                    "&f&l✽ &aHappy New Year &f&l✽\n" +
                    "&f\n" +
                    "&7The plugin has been enabled.";
        } else if (message.equalsIgnoreCase("stop")) {
            message = "\n" +
                    "&f&l✽ &r&fDeveloper: &chttps://vk.com/jdevs &f&l✽\n" +
                    "\n" +
                    "&f&l✽ &cHappy New Year &f&l✽\n" +
                    "\n" +
                    "&7The plugin has been disabled (v1.3.0)";
        } else if (message.equalsIgnoreCase("help")) {
            message = "&aHelp:\n" +
                    "\n" +
                    "&a/gifts reload - &fReload the plugin\n" +
                    "&a/gifts add Player - &fPlace a gift near the player\n" +
                    "&a/gifts put PlayerName Amount - &fSet your gift limit for the player\n" +
                    "&a/gifts check PlayerName - &fCheck player gift limit\n" +
                    "&a/gifts loot - &fGet help about the loot subcommand";
        } else if (message.equalsIgnoreCase("loot")) {
            message = "&aHelp from the loot subcommand:\n" +
                    "\n" +
                    "&a/gifts loot add santa/grinch Name Amount(from-to) Chance - &fAdd an item to gifts\n" +
                    "&a/gifts loot list santa/grinch - &fGet a list of items\n" +
                    "&a/gifts loot get santa/grinch Name - &fGet the item's ItemStack\n" +
                    "&a/gifts loot remove santa/grinch Name - &fRemove an item from gifts";
        } else if (message.equalsIgnoreCase("successfully")) {
            message = "&aSuccessfully. ";
            if (obj != null && obj.equalsIgnoreCase("restart")) {
                message = message + "A configuration reboot is required.";
            }
        } else if (message.equalsIgnoreCase("limit")) {
            message = "&aThe player has already collected a certain number of gifts! :(\n" +
                    "Add -f to the command to execute.";
        }
        else if (message.equalsIgnoreCase("error")) {
            if (!debug) {
                return null;
            }
            message = "&c[DEBUG] An error was found: ";
            if (obj != null) {
                message = message + obj;
            }
        }
        else {
            message = "Null";
        }
        return message;
    }
    private String getRU(String message, String obj) {
        if (message.equalsIgnoreCase("start")) {
            message = "\n" +
                    "&f&l✽ &r&fРазработчик: &ahttps://vk.com/jdevs &f&l✽\n" +
                    "\n" +
                    "&f&l✽ &aС новым годом &f&l✽\n" +
                    "\n" +
                    "&7Плагин был включён.";
        } else if (message.equalsIgnoreCase("stop")) {
            message = "\n" +
                    "&f&l✽ &r&fРазработчик: &chttps://vk.com/jdevs &f&l✽\n" +
                    "\n" +
                    "&f&l✽ &cС новым годом &f&l✽\n" +
                    "\n" +
                    "&7Плагин был выключен (v1.3.0)";
        } else if (message.equalsIgnoreCase("help")) {
            message = "&aПомощь:\n" +
                    "\n" +
                    "&a/gifts reload - &fПерезагрузить плагин\n" +
                    "&a/gifts add Игрок - &fЗаспавнить возле игрока подарок\n" +
                    "&a/gifts put ИмяИгрока Количество - &fПоставить свой лимит подарков для игрока\n" +
                    "&a/gifts check ИмяИгрока - &fПроверьте лимит подарков для игроков\n" +
                    "&a/gifts loot - &fПолучить помощь о подкоманде loot";
        } else if (message.equalsIgnoreCase("loot")) {
            message = "&aПомощь для подкоманды loot:\n" +
                    "\n" +
                    "&a/gifts loot add santa/grinch Название Количество(от-до) Шанс - &fДобавьте предмет в подарки\n" +
                    "&a/gifts loot list santa/grinch - &fПолучите названия предметов у подарков\n" +
                    "&a/gifts loot get santa/grinch Название - &fПолучите характеристику (ItemStack) предмета\n" +
                    "&a/gifts loot remove santa/grinch Название - &fУдалить предмет из подарков";
        } else if (message.equalsIgnoreCase("successfully")) {
            message = "&aУспешно. ";
            if (obj != null && obj.equalsIgnoreCase("restart")) {
                message = message + "&cТребуется перезагрузка конфигурации.";
            }
        } else if (message.equalsIgnoreCase("limit")) {
            message = "&aИгрок уже забрал определённое количество подарков! :(\n" +
                    "Добавьте -f для выполнения команды.";
        } else if (message.equalsIgnoreCase("error")) {
            if (!debug) {
                return null;
            }
            message = "&c[DEBUG] Была обнаружена ошибка: ";
            if (obj != null) {
                message = message + obj;
            }
        }
        else {
            message = "Здесь пусто.";
        }
        return message;
    }
}