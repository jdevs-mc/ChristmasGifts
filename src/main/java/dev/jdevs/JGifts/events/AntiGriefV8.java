package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public final class AntiGriefV8 implements Listener {
    Christmas plugin;
    public AntiGriefV8(Christmas plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    void Explode(BlockExplodeEvent event) {
        if (plugin.getValues().getGifts().isEmpty()) {
            return;
        }
        event.blockList().removeIf(b -> plugin.getValues().getGifts().containsKey(b.getLocation()));
    }
}
