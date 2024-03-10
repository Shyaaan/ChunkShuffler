package com.gmail.shayanbahrainy.chunkshuffler;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class Handler implements Listener {
    @EventHandler
    public void onPlayerSpawnChange(PlayerSpawnChangeEvent event) {
    	UUID UUID = event.getPlayer().getUniqueId();
    	Chunk SpawnChunk = event.getNewSpawn().getChunk();
    	ChunkShuffler.instance.movePlayerProtection(UUID, SpawnChunk.getX(), SpawnChunk.getZ());
    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
    	if (event.getCause() == TeleportCause.SPECTATE) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
    	if (event.getTo().getWorld() != Bukkit.getWorld("PlayerStorage")) {
    		return;
    	}
    	if (event.getTo().getBlockY() <= 0) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler 
    public void onPlayerJoin(PlayerJoinEvent event) {
    	if (ChunkShuffler.instance.getState()) {
    		ChunkShuffler.instance.storePlayer(event.getPlayer(), true);
    	}
    }
}