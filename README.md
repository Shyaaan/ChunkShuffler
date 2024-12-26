# ChunkShuffler 
A Minecraft plugin to shuffle around your chunks.
- /shuffle to enable/disable shuffling.
- /shuffletime to set the time between shuffles (in minutes)
- /shufflespeed to set the blocks set per tick (Recommended 30,000)

## Configuration (config.yml)
- Shuffle: true (Enable/Disable)
- Shuffletime: 1 (Time in minutes between shuffles)
- World: world (The world to shuffle)
- Chunks: 12 (How many chunks, too many will lag server)
- BlockSpeed: 30000 (Speed in Blocks per tick)

## Features 
While shuffling the player is sent to a "storage" world to prevent loss of data, and client-side lag. A chunk may not be shuffled if:
- A player is currently inside of it
- A player has set their spawn inside of it (I.E. Bed)  
You may see a warning on the console warning that the plugin has registered a listener for PlayerSpawnChange event, there is no way to get around this.

## Contributing
As this is a Paper plugin, you'll need to setup your environment to build the plugin not just as a regular Java app.
Here is a handy tutorial:
[Eclipse Bukkit Tutorial](https://bukkit.fandom.com/wiki/Plugin_Tutorial_(Eclipse))
### What is the difference between Spigot, Paper and Bukkit?
Bukkit started this 'family' when it forked the code for Minecraft servers, and provided an API to create plugins (such as this one). Spigot is a fork of Bukkit, with more features, and optimizations. The Bukkit is no longer maintained, but Spigot continues its legacy. Paper came along and added many crafty optimizations, and fixes, even to bugs left in by Mojang! Paper is much faster than Spigot, and with the many other innovative projects from the Paper team, Paper is the future of this line of Minecraft plugins.
## Dependency/ies
This plugin depends on the WorldEdit API because they have the resources to keep updating with every change to Minecraft's internal code, I don't. 
## Technical Challenges
At 30,000 blocks per tick, and 20 TPS, this plugin operates at 600,000 blocks per second. We operate on a scheduled task system where we do incremental work on reading/writing the blocks, so the server doesn't crash. This system has to be fast, as the user is in a storage world, and we want to minimize the interruption to gameplay.
