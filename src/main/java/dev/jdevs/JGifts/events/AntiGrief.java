package dev.jdevs.JGifts.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;


import static dev.jdevs.JGifts.events.FallGifts.gifts;

public class AntiGrief implements Listener {
    @EventHandler
    void PistonExtend(BlockPistonExtendEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        for (Block ok : event.getBlocks()) {
            if (gifts.containsKey(ok.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    void PistonRetract(BlockPistonRetractEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        for (Block ok : event.getBlocks()) {
            if (gifts.containsKey(ok.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (gifts.containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void Explode(EntityExplodeEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        event.blockList().removeIf(b -> gifts.containsKey(b.getLocation()));
    }
    @EventHandler
    void Explode(BlockExplodeEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        event.blockList().removeIf(b -> gifts.containsKey(b.getLocation()));
    }

    @EventHandler
    void Explode(BlockDamageEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (gifts.containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    void Explode(BlockBreakEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (gifts.containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    void BlockBreak(BlockBreakEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        Block b = event.getBlock();
        if (gifts.containsKey(b.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onWaterFlow(BlockFromToEvent event) {
        if (gifts.isEmpty()) {
            return;
        }
        Material type = event.getBlock().getType();
        if (type.toString().contains("WATER") || type.toString().contains("LAVA")) {
            if (gifts.containsKey(event.getToBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
