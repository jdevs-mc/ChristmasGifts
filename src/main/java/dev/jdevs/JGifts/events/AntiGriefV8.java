package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public final class AntiGriefV8 implements Listener {
    private final Values values;
    public AntiGriefV8(Christmas plugin) {
        this.values = plugin.getValues();
    }
    @EventHandler
    void Explode(BlockExplodeEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        event.blockList().removeIf(b -> values.getGifts().containsKey(b.getLocation()));
    }
}
