package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.made.CustomMessages;
import dev.jdevs.JGifts.utils.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static dev.jdevs.JGifts.Settings.HologramType;
import static dev.jdevs.JGifts.loots.Loot.dropLoot;
import static dev.jdevs.JGifts.supports.WG.setBlock;

public class UseGifts implements Listener {
    @EventHandler
    public void ClickCommand(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            Location block = event.getClickedBlock().getLocation();
            Player p = event.getPlayer();
            if (!FallGifts.gifts.containsKey(block)) {
                return;
            }
            if (!FallGifts.gifts.get(block).equals(p)) {
                return;
            }
            if (HologramType != null) {
                if (HologramType.contains("decentholograms")) {
                    FallGifts.decentHolograms.get(block).delete();
                    FallGifts.decentHolograms.remove(block);
                }
                else {
                    FallGifts.holographicDisplays.get(block).delete();
                    FallGifts.holographicDisplays.remove(block);
                }
            }
            FallGifts.gifts.remove(block);
            // Drop loot
            dropLoot(block);
            //
            for (String msg : CustomMessages.success) {
                Message.sendMessage(event.getPlayer(), msg);
            }
            org.bukkit.block.Block b = block.getBlock();
            setBlock(block, b);
        }
    }
}