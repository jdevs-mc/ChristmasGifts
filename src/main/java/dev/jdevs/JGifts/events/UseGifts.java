package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class UseGifts implements Listener {
    Christmas plugin;
    private final Values values;
    public UseGifts(Christmas plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }
    @EventHandler
    public void ClickCommand(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            Location block = event.getClickedBlock().getLocation();
            Player p = event.getPlayer();
            if (!values.getGifts().containsKey(block)) {
                return;
            }
            if (!values.getGifts().get(block).equals(p.getUniqueId())) {
                return;
            }
            String hologramType = values.getHologramType();
            if (hologramType != null && !hologramType.contains("null")) {
                if (hologramType.contains("decentholograms")) {
                    values.getDecentHolograms().get(block).delete();
                    values.getDecentHolograms().remove(block);
                }
                else {
                    values.getHolographicDisplays().get(block).delete();
                    values.getHolographicDisplays().remove(block);
                }
            }
            // Drop loot
            plugin.getLoad().dropLoot(block);
            //
            for (String msg : values.getSuccess()) {
                plugin.getMessages().sendMessage(event.getPlayer(), msg, block);
            }
            values.getGifts().remove(block);
            org.bukkit.block.Block b = block.getBlock();
            plugin.getWg().setBlock(block, b);
        }
    }
}