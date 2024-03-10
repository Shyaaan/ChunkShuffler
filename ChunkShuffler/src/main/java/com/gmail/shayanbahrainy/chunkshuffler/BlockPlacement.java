package com.gmail.shayanbahrainy.chunkshuffler;

import org.bukkit.block.data.BlockData;
import org.bukkit.Location;

public class BlockPlacement {
	public BlockData BlockData;
	public Location Location;
	public BlockPlacement(BlockData BD, Location L) {
		BlockData = BD;
		Location = L;
	}
}
