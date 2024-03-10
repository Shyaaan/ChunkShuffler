package com.gmail.shayanbahrainy.chunkshuffler;


import java.util.Random;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;


public class LoadingGenerator extends ChunkGenerator {
	public SimplexOctaveGenerator Generator = new SimplexOctaveGenerator(new Random(), 8);
	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}
	@Override
	public void generateSurface(WorldInfo worldInfo, Random random,int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData) {


        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
            	int height = (int) Generator.noise(chunkX + x, chunkZ + z, 0.01, 0.01);
                Material M = random.nextBoolean() ? Material.DIAMOND_BLOCK : Material.HONEY_BLOCK;
            	chunkData.setBlock(x, height, z, M);
            }
        }
	}

}