package com.gmail.shayanbahrainy.chunkshuffler;

import java.util.ArrayList;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;

public class ChuffleState implements Runnable{
	private int i;
	private ArrayList<BlockPlacement> DataMap; //Map containing which block each location needs to be set to
	private int in;
	private int taskid;
	private com.sk89q.worldedit.bukkit.BukkitWorld BukkitWorld;

	public ChuffleState(int increment, ArrayList<BlockPlacement> BlockData) {
		i = 0;
		in = increment;
		DataMap = BlockData;
		BukkitWorld = new com.sk89q.worldedit.bukkit.BukkitWorld((DataMap.getFirst().Location).getWorld());
	}
	@Override
	public void run() {
		if (ChunkShuffler.instance.isShuffleEnabled()) {
			int starti = i;
			try (EditSession EditSession = WorldEdit.getInstance().newEditSession(BukkitWorld)){
			
			while (i < starti + in) {
				if (i == DataMap.size() - 1) {				
					Bukkit.getScheduler().cancelTask(taskid);
					DataMap.clear();
					ChunkShuffler.instance.setState(false);
					for (Player p : Bukkit.getOnlinePlayers()) {
						ChunkShuffler.instance.restorePlayer(p, true);
					}
					break;
				}
				//Location L = Locations.get(i);
				Location L = DataMap.get(i).Location;
			    BlockData BD = DataMap.get(i).BlockData;
			    try {
			    	//L.getWorld().setBlockData(L, BD);
			    	EditSession.setBlock(BlockVector3.at(L.getX(), L.getY(), L.getZ()),BukkitAdapter.adapt(BD));
			    }
			    catch (Exception e) {
			    	Bukkit.getLogger().info(e.getMessage());;
			    	Bukkit.getLogger().info("Error Key Id: " + String.valueOf(i));
			    }
			    
				i++;
			}
			EditSession.close();
			}
		}
	} 	
	public void setTaskId(int id) {
		taskid = id;
	}
}
