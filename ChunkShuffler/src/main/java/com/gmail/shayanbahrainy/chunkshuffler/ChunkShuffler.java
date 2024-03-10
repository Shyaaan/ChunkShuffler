package com.gmail.shayanbahrainy.chunkshuffler;



import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;





public class ChunkShuffler extends JavaPlugin {
	
	public static ChunkShuffler instance = null;
	private boolean Enabled = true;
	private World ShuffleWorld = null;
	private double ShuffleTime = 10;
	private BukkitTask BT;
	private List<List<Integer>> ProtectedChunks;
	private FileConfiguration FC;
	private int ChunkQuantity;
	
	private Map<UUID,Location> PlayerLocations = new HashMap<UUID,Location>();
	private Map<UUID,GameMode> PlayerGameModes = new HashMap<UUID,GameMode>();
    private Map<UUID,Boolean> PlayerStorageStates = new HashMap<UUID,Boolean>();
    
    private String DataBaseUri = "jdbc:sqlite:plugins/ChunkShuffler/Data.db";
	private int ShuffleSpeed;
	private boolean ShuffleState;
    
    @Override
    public void onEnable() {
    	instance = this;
    	
    	boolean DatabaseSuccess = LoadDatabase();
    	
    	if (!DatabaseSuccess) {
    		Bukkit.getLogger().warning("Database failed, " + this.getName() + " shutting down.");
    		Bukkit.getServer().getPluginManager().disablePlugin(this);
    		return;
    	}
    	
    	WorldCreator WC = new WorldCreator("PlayerStorage");
    	WC.generateStructures(false);
    	WC.generator(new LoadingGenerator());
    	WC.createWorld();
    	
    	World PlayerStorage = Bukkit.getWorld("PlayerStorage");
    	PlayerStorage.setPVP(false);
    	PlayerStorage.setSpawnFlags(false, false);
    	
    	Bukkit.getServer().getPluginManager().registerEvents(new Handler(), this);  
    	
    	this.saveDefaultConfig();
    	FC = this.getConfig(); 
    	ShuffleWorld = Bukkit.getWorld(FC.getString("World"));
    	Enabled = FC.getBoolean("Shuffle");
    	ShuffleTime = FC.getDouble("Shuffletime");
    	ChunkQuantity = FC.getInt("Chunks");
    	ShuffleSpeed = FC.getInt("BlockSpeed");
    	Bukkit.getLogger().info("Chunk Shuffling: " + String.valueOf(Enabled));
    	Bukkit.getLogger().info("Shuffling Time: " + String.valueOf(ShuffleTime) + " Minutes.");
    	
    	ShuffleChunks SC = new ShuffleChunks();
    	BT = Bukkit.getScheduler().runTaskTimer(this, SC, 0, Math.round(60 * 20 * ShuffleTime));
    }
    @Override
    public void onDisable() {
    	try {
    		FC.set("Shuffle", Enabled);
    		FC.set("Shuffletime", ShuffleTime);
    		FC.set("ProtectedChunks", ProtectedChunks);
    		FC.set("BlockSpeed", ShuffleSpeed);
			FC.save("config.yml");
		} catch (IOException e) {
			Bukkit.getLogger().warning(e.getMessage());;
		}
    }
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
	    return new LoadingGenerator();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("shuffle")) {
    		Enabled = !Enabled;
    		sender.sendMessage("Shuffle is now set to: " + String.valueOf(Enabled));
    		return true;
    	}
    	if (cmd.getName().equalsIgnoreCase("loadingworld")) {
    		Player Player = null;
    		if (args.length < 1 && !(sender instanceof Player)) {
    			sender.sendMessage("Specify or be a player!");
    			return false;
    		}
    		if (args.length > 0) {
    			Player = Bukkit.getPlayer(args[0]);
    			if (Player == null) {
    				sender.sendMessage("Player not found!");
    				return false;
    			}
    		}
    		if (args.length < 1) {
    			Player = (Player) sender;
    		}
    		if (PlayerStorageStates.containsKey(Player.getUniqueId())) {
    			restorePlayer(Player,false);
    		}
    		else {
    			storePlayer(Player,false);
    		}
    		return true;
    	}
    	if (cmd.getName().equalsIgnoreCase("shuffletime")) {
    		if (args.length == 0) {
    			sender.sendMessage("Shuffling every " + ShuffleTime + " minutes.");
    			return true;
    		} 
    		try {
        		ShuffleTime = Double.parseDouble(args[0]);
        		Bukkit.getScheduler().cancelTask(BT.getTaskId());
        		ShuffleChunks SC = new ShuffleChunks();
            	BT = Bukkit.getScheduler().runTaskTimer(this, SC, 0, Math.round(60 * 20 * ShuffleTime));
    		}
    		catch (NumberFormatException e){
    			Bukkit.getLogger().warning("Invalid time supplied: " + args[0] + " by: " + sender.getName());
    			sender.sendMessage("Wrong time format supplied, please try again: " + args[0]);
    			return false;
    		}
    		sender.sendMessage("Time between shuffles is now: " + args[0] + " minutes.");
    		return true;
    	}
    	if (cmd.getName().equalsIgnoreCase("shufflespeed")) {
    		if (args.length == 0) {
    			sender.sendMessage("Shuffling at " + ShuffleSpeed + " blocks per tick.");
    			return true;
    		}
    		try {
    			ShuffleSpeed = Integer.parseInt(args[0]);
    			sender.sendMessage("Shuffling at " + ShuffleSpeed + " blocks per tick.");
    			return true;
    		}
    		catch (NumberFormatException e) {
    			Bukkit.getLogger().warning("Invalid speed supplied: " + args[0] + " by: " + sender.getName());
    			sender.sendMessage("Wrong speed format supplied, please try again: " + args[0]);
    			return false;
    		}
    	}
    	return false;
    }
    public boolean isShuffleEnabled() {
    	return Enabled;
    }
    public double getShuffleTime() {
    	return ShuffleTime;
    }
    public ArrayList<Chunk> getPlayedChunks() {
    	ArrayList<Chunk> Chunks = new ArrayList<Chunk>();
    	 for (Player player : Bukkit.getOnlinePlayers()) {
    		
    		 if (!player.getWorld().equals(ChunkShuffler.instance.ShuffleWorld)) {
    			 continue;
    		 }
             int renderDistance = player.getClientViewDistance();

             int centerX = player.getLocation().getChunk().getX();
             int centerZ = player.getLocation().getChunk().getZ();

             for (int x = centerX - renderDistance; x <= centerX + renderDistance; x++) {
                 for (int z = centerZ - renderDistance; z <= centerZ + renderDistance; z++) {
                     Chunk chunk = player.getWorld().getChunkAt(x, z);
                     
                     if (Chunks.contains(chunk)) {
                         continue;
                     }
                     if (x == centerX && z == centerZ) {
                    	 continue;
                     }
                     if (isChunkProtected(x,z)) {
                    	 continue;
                     }
                     Chunks.add(chunk); 
                 }
             }
         }
    	 int n = 0;
    	 while (Chunks.size() % 2 == 1) {
    		 Chunk chunk = ShuffleWorld.getChunkAt(n, n);
    		 if (isChunkProtected(n,n)) continue;
    		 if (!Chunks.contains(chunk)) {
    			 Chunks.add(chunk);
    		 }
    		 else {
    			 n++;
    		 }
    	 }
    	return Chunks;
    }
    public void storePlayer(Player Player, boolean Natural) {
    	UUID UUID = Player.getUniqueId();
    	boolean isNatural;
    	if (PlayerStorageStates.containsKey(UUID)) {
    		isNatural = PlayerStorageStates.get(UUID);
    	}
    	else {
    		PlayerLocations.put(UUID, Player.getLocation());
    		PlayerGameModes.put(UUID, Player.getGameMode());
    		PlayerStorageStates.put(UUID, Natural);
    		Player.setGameMode(GameMode.SPECTATOR);
    		Player.teleport(Bukkit.getWorld("PlayerStorage").getSpawnLocation());
    		return;
    	}
    	if (isNatural && !Natural) {
        	PlayerStorageStates.put(UUID,false);
    	}
    }
    public void restorePlayer(Player Player, boolean Natural) {
    	UUID UUID = Player.getUniqueId();
    	if (!PlayerStorageStates.containsKey(UUID)) {
    		return;
    	}
    	boolean isNatural = PlayerStorageStates.get(UUID);
    	if (!Natural) {
    		Player.teleport(PlayerLocations.get(UUID));
    		Player.setGameMode(PlayerGameModes.get(UUID));
    		PlayerStorageStates.remove(UUID);
    		return;
    	}
    	if (Natural && isNatural) {
    		Player.teleport(PlayerLocations.get(UUID));
    		Player.setGameMode(PlayerGameModes.get(UUID));
    		PlayerStorageStates.remove(UUID);
    		return;
    	}
    }
    
    public boolean LoadDatabase() {
   	 String PlayerData = "CREATE TABLE IF NOT EXISTS ProtectedChunks (\n"
             + "	Player text PRIMARY KEY,\n"
             + "	Chunk text NOT NULL\n"
             + ");";
   	 String MemoryData = "CREATE TABLE IF NOT EXISTS MemoryUsage (\n"
             + "	MemoryPerChunk int NOT NULL\n"
             + ");";
     try {
    	 Connection conn = DriverManager.getConnection(DataBaseUri);
         Statement PlayerStatement = conn.createStatement();
         PlayerStatement.execute(PlayerData);
         Statement MemoryStatement = conn.createStatement();
         MemoryStatement.execute(MemoryData);
     } catch (SQLException e) {
        Bukkit.getLogger().warning(e.getMessage());
        return false;
     }
	return true;
    }
    public void addRun(int MemoryUsagePerChunk) {
    	String AddRun = "INSERT INTO MemoryUsage (MemoryPerChunk) \n" 
    					+ "VALUES (%s)".formatted(MemoryUsagePerChunk);
        try (Connection conn = DriverManager.getConnection(DataBaseUri);
                Statement addRunStatement = conn.createStatement()) {
        		addRunStatement.execute(AddRun);
        } catch (SQLException e) {
               Bukkit.getLogger().warning(e.getMessage());
        }
    }
    /*
     * Returns -1 for error/no record.
     */
    public int estimatedMemoryPerChunk() {
    	String getAverage = "select avg(MemoryPerChunk) from MemoryUsage;"; 
		try (Connection conn = DriverManager.getConnection(DataBaseUri);
		        Statement getAverageStatement = conn.createStatement()) {
				getAverageStatement.execute(getAverage);
				ResultSet RS = getAverageStatement.getResultSet();
				return (int) RS.getFloat(1);
		} catch (SQLException e) {
		       Bukkit.getLogger().warning(e.getMessage());
		       return -1;
		}
    }
    public int recommendedChunkQuantity() {
    	int memoryPerChunk = estimatedMemoryPerChunk();
    	if (memoryPerChunk == -1) {
    		return 6;
    	}
    	Runtime R = Runtime.getRuntime();
    	long FreeMemory = R.freeMemory();
    	FreeMemory = FreeMemory/1000;
    	FreeMemory = (long) (FreeMemory * .01/100.0); //Only use .01% of memory
    	Bukkit.getLogger().info("Free Memory: " + FreeMemory);
    	Bukkit.getLogger().info("Estimated chunks before correction: " + String.valueOf(FreeMemory/memoryPerChunk));
    	return (int) ((FreeMemory/memoryPerChunk)/5.6);
    }
    public int getChunkQuantity() {
    	return ChunkQuantity;
    }
    public boolean isChunkProtected(int ChunkX, int ChunkZ) {
    	String KeyId = String.valueOf(ChunkX) + "," + String.valueOf(ChunkZ);
    	String sql = "SELECT COUNT(*) FROM ProtectedChunks WHERE Chunk = '" + KeyId + "';";
    	try (Connection conn = DriverManager.getConnection(DataBaseUri)){
                Statement Statement = conn.createStatement();
                Statement.execute(sql);
                ResultSet RS = Statement.getResultSet();
                Integer results = RS.getInt(1);
                if (results == 1) {
                	return true;
                }
                return false;
    	}
    	catch (SQLException e) {
           Bukkit.getLogger().warning(e.getMessage());
           return true;
        }
    	
    }
    public boolean movePlayerProtection(UUID UUID, int ChunkX, int ChunkY) {
    	String KeyId = String.valueOf(ChunkX) + "," + String.valueOf(ChunkY);
    	String sql = "REPLACE INTO ProtectedChunks (player, chunk) VALUES ('%s','%s');".formatted(UUID.toString(),KeyId);
    	
    	try (Connection conn = DriverManager.getConnection(DataBaseUri)){
            Statement Statement = conn.createStatement();
            Statement.execute(sql);
    	}
    	catch (SQLException e) {
    		Bukkit.getLogger().warning(e.getMessage());
    		return true;
    	}
    	
    	return true;
    }
    public World getShuffleWorld() {
    	return ShuffleWorld;
    }
	public int getSpeed() {
		return ShuffleSpeed;
	}
	public void setState(boolean b) {
		ShuffleState = b;
	}
	public boolean getState() {
		return ShuffleState;
	}
}