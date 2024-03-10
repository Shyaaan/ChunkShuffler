package com.gmail.shayanbahrainy.chunkshuffler;


import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class BlockArrangement implements Runnable{
	
	private ArrayList<BlockPlacement> Placements; 
	private Map<Chunk,Chunk> CMap;
	private Chunk Key;
	private int taskid;
	private int KeyId;
	private int CLimit;
	private boolean SelfDestruct;
	
	public BlockArrangement(Map<Chunk,Chunk> ChunkMap, int ChunkLimit) {
		CMap = ChunkMap;
		CLimit = ChunkLimit;
		if (CMap.size() == 0) {
			SelfDestruct = true;
		}
		if (CLimit > CMap.size()) {
			CLimit = CMap.size();
		}
		KeyId = 0;
		Placements = new ArrayList<BlockPlacement>();
	}
	
	@Override
	public void run() {
		if (SelfDestruct) {
			Bukkit.getScheduler().cancelTask(taskid);
			return;
		}
		if (!ChunkShuffler.instance.isShuffleEnabled()) {
			return;
		}
		ChunkShuffler.instance.setState(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			ChunkShuffler.instance.storePlayer(p, true);
		}
		World World = ChunkShuffler.instance.getShuffleWorld();
		Key = (Chunk) CMap.keySet().toArray()[KeyId];
		Location DestinationOrigin = new Location(World,(double) Key.getX() * 16, 0.0, (double) Key.getZ() * 16);
		Chunk OriginChunk = CMap.get(Key);
		for (int x = 0; x <= 15; x++) {
			for (int z = 0; z <= 15; z++) {
				for (int y = World.getMinHeight(); y <= World.getMaxHeight(); y++) {
						DestinationOrigin = new Location(World,(double) Key.getX() * 16, 0.0, (double) Key.getZ() * 16);
						Location DestinationLocation = DestinationOrigin.add(x,y,z);
						BlockData BD = OriginChunk.getBlock(x, y, z).getBlockData();
						BlockPlacement BP = new BlockPlacement(BD,DestinationLocation);
						Placements.add(BP);
					}
				}
			}
		if (KeyId == CLimit) {
			Bukkit.getScheduler().cancelTask(taskid);
			ChuffleState CS = new ChuffleState(ChunkShuffler.instance.getSpeed(), Placements);
			BukkitTask BT = Bukkit.getScheduler().runTaskTimer(ChunkShuffler.instance,CS,2,2);
			
			CS.setTaskId(BT.getTaskId());
		}
		KeyId++;
	} 	
	public void setTaskId(int id) {
		taskid = id;
	}
}
