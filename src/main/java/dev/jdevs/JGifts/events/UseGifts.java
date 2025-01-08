package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.loots.Load;
import dev.jdevs.JGifts.supports.WG;
import dev.jdevs.JGifts.utils.Message;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class UseGifts implements Listener {
    private final Values values;
    private final Load load;
    private final Message messages;
    private final WG wg;
    private final int version;
    public UseGifts(Christmas plugin) {
        version = plugin.getVersion_mode();
        load = plugin.getLoad();
        messages = plugin.getMessages();
        wg = plugin.getWg();
        values = plugin.getValues();
    }
    @EventHandler
    public void ClickCommand(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            Block b = event.getClickedBlock();
            Location loc = b.getLocation();
            Player p = event.getPlayer();
            if (!values.getGifts().containsKey(loc)) {
                return;
            }
            if (!values.getGifts().get(loc).equals(p.getUniqueId())) {
                return;
            }
            delHolograms(loc);
            // Drop loot
            load.dropLoot(loc);
            //
            String locale = "";
            if (values.getMessageMode() == 2) {
                if (version <= 12) {
                    //noinspection deprecation
                    locale = p.spigot().getLocale().toLowerCase();
                } else {
                    locale = p.getLocale().toLowerCase();
                }
            }
            if (values.getMessageMode() == 2 && values.getSuccessful().containsKey(locale)) {
                for (String msg : values.getSuccessful().get(locale)) {
                    messages.sendMessage(p, msg, loc);
                }
            }
            else {
                for (String msg : values.getSuccessful().get("default")) {
                    messages.sendMessage(p, msg, loc);
                }
            }
            values.getGifts().remove(loc);
            wg.setBlock(loc, b);
        }
    }
    private void delHolograms(Location block) {
        if (!values.getDecentHolograms().isEmpty() || !values.getHolographicDisplays().isEmpty()) {
            if (values.getDecentHolograms().get(block) != null) {
                values.getDecentHolograms().get(block).delete();
                values.getDecentHolograms().remove(block);
            }
            else if (values.getHolographicDisplays().get(block) != null) {
                values.getHolographicDisplays().get(block).delete();
                values.getHolographicDisplays().remove(block);
            }
        }
    }
}