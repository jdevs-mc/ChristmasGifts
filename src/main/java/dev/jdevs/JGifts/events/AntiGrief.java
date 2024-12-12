package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class AntiGrief implements Listener {
    private final int version_mode;
    private final Values values;
    public AntiGrief(Christmas plugin) {
        version_mode = plugin.getVersion_mode();
        values = plugin.getValues();
    }
    @EventHandler
    void PistonExtend(BlockPistonExtendEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        for (Block ok : event.getBlocks()) {
            if (values.getGifts().containsKey(ok.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    void PistonRetract(BlockPistonRetractEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        if (version_mode >= 8) {
            for (Block ok : event.getBlocks()) {
                if (values.getGifts().containsKey(ok.getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
        else {
            if (values.getGifts().containsKey(event.getRetractLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (values.getGifts().containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void Explode(EntityExplodeEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        event.blockList().removeIf(b -> values.getGifts().containsKey(b.getLocation()));
    }

    @EventHandler
    void Explode(BlockDamageEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (values.getGifts().containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    void Explode(BlockBreakEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (values.getGifts().containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    void BlockBreak(BlockBreakEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (values.getGifts().containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onWaterFlow(BlockFromToEvent event) {
        if (values.getGifts().isEmpty()) {
            return;
        }
        Material type = event.getBlock().getType();
        if (type.toString().contains("WATER") || type.toString().contains("LAVA")) {
            if (values.getGifts().containsKey(event.getToBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
