package com.gmail.shayanbahrainy.chunkshuffler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;



public class ShuffleChunks implements Runnable{
	private Random random;
	private Map<Chunk,Chunk> ChunkMap;
	private int taskId;
	public ShuffleChunks() {
		random = new Random();
	}
	
	@Override
	public void run() {
		if (ChunkShuffler.instance.isShuffleEnabled()) {
			Runtime Runtime = java.lang.Runtime.getRuntime();
			
			Runtime.gc();
			
			
			Collection<? extends Player> Players = Bukkit.getOnlinePlayers();
			
			if (Players.size() == 0) {
				return;
			}
			
			if (Bukkit.getWorlds().get(0).getPlayers().size() == 0) {
				return;
			}
			
			ChunkMap = generateChunkMap();
			BlockArrangement BlockArrangement = new BlockArrangement(ChunkMap,ChunkShuffler.instance.getChunkQuantity());
			BukkitTask BT = Bukkit.getScheduler().runTaskTimer(ChunkShuffler.instance, BlockArrangement, 1, 2);
			BlockArrangement.setTaskId(BT.getTaskId());
			
			Runtime.gc();
		}
	}	
	private Map<Chunk,Chunk> generateChunkMap(){
		Map<Chunk,Chunk> ChunkMap = new HashMap<Chunk,Chunk>();
		ArrayList<Chunk> ChunkList = ChunkShuffler.instance.getPlayedChunks();
		Chunk[] Chunks = new Chunk[ChunkList.size()];
		for (int i = 0; i < ChunkList.size(); i++) {
			Chunks[i] = ChunkList.get(i);
		}
        int n = Chunks.length;
        int i;
        for (i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            Chunk temp = Chunks[i];
            Chunks[i] = Chunks[j];
            Chunks[j] = temp;
        }
        for (i = 0; i < Chunks.length; i += 2) {
            Chunk element1 = Chunks[i];
            Chunk element2 = Chunks[i + 1];
            ChunkMap.put(element1, element2);
            ChunkMap.put(element2, element1);
        }
        if (ChunkShuffler.instance.isDebug()) {
        	Bukkit.getLogger().info("ChunkMap: " + ChunkMap.toString());
        }
		return ChunkMap;
	}
	public void setTaskId(int TaskId) {
		taskId = TaskId;
	}
	public int getTaskId() {
		return taskId;
	}
}
