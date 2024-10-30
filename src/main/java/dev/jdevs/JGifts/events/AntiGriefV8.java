package dev.jdevs.JGifts.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import static dev.jdevs.JGifts.events.FallGifts.gifts;

public class AntiGriefV8 implements Listener {
    @EventHandler
    void Explode(BlockExplodeEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        event.blockList().removeIf(b -> gifts.containsKey(b.getLocation()));
    }
}
