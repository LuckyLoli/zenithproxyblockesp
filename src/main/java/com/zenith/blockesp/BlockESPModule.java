package com.zenith.blockesp;

import com.github.rfresh2.EventConsumer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.cache.data.chunk.Chunk;
import com.zenith.discord.Embed;
import com.zenith.event.client.ClientBotTick;
import com.zenith.module.api.Module;
import com.zenith.util.Color;
import com.zenith.util.timer.Timer;
import com.zenith.util.timer.Timers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Globals.CACHE;
import static com.zenith.Globals.DISCORD;

public class BlockESPModule extends Module {
    private final Map<ChunkPos, Long> alertedChunks = new ConcurrentHashMap<>();
    private final Timer checkTimer = Timers.tickTimer();
    private static final int CHECK_INTERVAL_TICKS = 100; // Check every 5 seconds (100 ticks)
    private static final long ALERT_COOLDOWN_MS = 300000; // 5 minutes cooldown before re-alerting same chunk
    
    private String discordChannelId = null;
    private String accountOwnerRoleId = null;
    
    @Override
    public boolean enabledSetting() {
        return BlockESPPlugin.PLUGIN_CONFIG.isEnabled();
    }
    
    @Override
    public List<EventConsumer<?>> registerEvents() {
        return List.of(
            of(ClientBotTick.class, this::handleBotTick)
        );
    }
    
    private void handleBotTick(ClientBotTick event) {
        if (checkTimer.tick(CHECK_INTERVAL_TICKS)) {
            scanChunksForBases();
        }
    }
    
    private BlockESPConfig getConfig() {
        return BlockESPPlugin.PLUGIN_CONFIG;
    }
    
    @Override
    public void onEnable() {
        info("BlockESP Module Enabled - Scanning chunk cache!");
        info("Trigger amount: " + getConfig().getTriggerAmount());
        info("Tracking " + getConfig().getTrackedBlocks().size() + " block types");
        loadDiscordConfig();
    }
    
    private void loadDiscordConfig() {
        try {
            Path configPath = Path.of("config.json");
            if (!Files.exists(configPath)) {
                info("Config file not found at: " + configPath.toAbsolutePath());
                return;
            }
            
            String json = Files.readString(configPath);
            JsonObject config = JsonParser.parseString(json).getAsJsonObject();
            
            if (config.has("discord")) {
                JsonObject discord = config.getAsJsonObject("discord");
                discordChannelId = discord.has("channelId") ? discord.get("channelId").getAsString() : null;
                accountOwnerRoleId = discord.has("accountOwnerRoleId") ? discord.get("accountOwnerRoleId").getAsString() : null;
                
                info("Discord integration loaded:");
                info("  Channel ID: " + (discordChannelId != null ? discordChannelId : "Not set"));
                info("  Owner Role ID: " + (accountOwnerRoleId != null ? accountOwnerRoleId : "Not set"));
            } else {
                info("Discord section not found in config.json");
            }
        } catch (Exception e) {
            error("Failed to load Discord config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        info("BlockESP Module Disabled");
        alertedChunks.clear();
    }
    
    private void scanChunksForBases() {
        BlockESPConfig config = getConfig();
        int triggerAmount = config.getTriggerAmount();
        
        try {
            // Get all loaded chunks from the cache
            var chunkCache = CACHE.getChunkCache();
            if (chunkCache == null) {
                info("Chunk cache is null");
                return;
            }
            
            // Get snapshot of chunks to avoid concurrent modification
            List<Chunk> chunks = new ArrayList<>(chunkCache.getCache().values());
            
            if (chunks.isEmpty()) {
                info("No chunks loaded in cache");
                return;
            }
            
            info("Scanning " + chunks.size() + " loaded chunks...");
            
            long currentTime = System.currentTimeMillis();
            
            for (Chunk chunk : chunks) {
                ChunkPos pos = new ChunkPos(chunk.getX(), chunk.getZ());
                
                // Check cooldown - skip if alerted recently
                Long lastAlertTime = alertedChunks.get(pos);
                if (lastAlertTime != null && (currentTime - lastAlertTime) < ALERT_COOLDOWN_MS) {
                    continue; // Still on cooldown
                }
                
                Map<String, Integer> blockCounts = new ConcurrentHashMap<>();
                
                // Scan block entities in this chunk
                var blockEntities = chunk.getBlockEntities();
                if (blockEntities != null && !blockEntities.isEmpty()) {
                    for (var blockEntity : blockEntities) {
                        try {
                            String blockName = getBlockEntityName(blockEntity.getType());
                            
                            if (blockName != null && config.isBlockTracked(blockName)) {
                                blockCounts.merge(blockName, 1, Integer::sum);
                            }
                        } catch (Exception e) {
                            // Ignore individual block entity errors
                        }
                    }
                }
                
                int totalBlocks = blockCounts.values().stream().mapToInt(Integer::intValue).sum();
                
                if (totalBlocks >= triggerAmount) {
                    alertedChunks.put(pos, currentTime);
                    sendBaseAlert(pos, blockCounts, totalBlocks);
                }
            }
        } catch (Exception e) {
            error("Error scanning chunks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendBaseAlert(ChunkPos chunkPos, Map<String, Integer> blockCounts, int totalBlocks) {
        int centerX = chunkPos.x * 16 + 8;
        int centerZ = chunkPos.z * 16 + 8;
        
        StringBuilder message = new StringBuilder();
        message.append("§c§l[BlockESP] Possible Base Found§r\n");
        message.append("§eCoordinates:§r\n");
        message.append("§fX: §a").append(centerX).append("§r\n");
        message.append("§fZ: §a").append(centerZ).append("§r\n");
        message.append("§eTotal Blocks: §a").append(totalBlocks).append("§r\n");
        message.append("§eBlocks found:§r\n");
        
        blockCounts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach(entry -> {
                String blockName = formatBlockName(entry.getKey());
                message.append("§f  ").append(entry.getValue()).append("x §b").append(blockName).append("§r\n");
            });
        
        info(message.toString());
        
        // Send Discord alert if configured
        sendDiscordAlert(centerX, centerZ, totalBlocks, blockCounts);
    }
    
    private void sendDiscordAlert(int x, int z, int totalBlocks, Map<String, Integer> blockCounts) {
        if (discordChannelId == null || discordChannelId.isEmpty()) {
            return; // Discord not configured
        }
        
        try {
            // Build block list for embed field
            StringBuilder blockList = new StringBuilder();
            blockCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    blockList.append(entry.getValue()).append("x ").append(formatBlockName(entry.getKey())).append("\n");
                });
            
            // Create Discord embed
            Embed embed = Embed.builder()
                .title("🚨 Possible Base Found!")
                .errorColor()
                .addField("Coordinates", "||X: " + x + " Z: " + z + "||", false)
                .addField("Total Blocks", String.valueOf(totalBlocks), true)
                .addField("Blocks Found", blockList.toString(), false);
            
            // Optional role ping message (sent separately from embed)
            String pingMessage = null;
            if (getConfig().isOwnerPingEnabled() && accountOwnerRoleId != null && !accountOwnerRoleId.isEmpty()) {
                pingMessage = "<@&" + accountOwnerRoleId + ">";
            }
            
            // Send via ZenithProxy's Discord bot with embed
            DISCORD.sendEmbedMessage(pingMessage, embed);
            
            info("Discord alert sent to channel " + discordChannelId + " for base at [" + x + ", " + z + "]");
        } catch (Exception e) {
            error("Failed to send Discord alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String formatBlockName(String blockName) {
        String[] words = blockName.replace("_", " ").toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            if (words[i].length() > 0) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1));
                }
            }
        }
        return result.toString();
    }
    
    private String getBlockEntityName(Object type) {
        if (type == null) return null;
        
        String typeName = type.toString();
        
        // Map BlockEntityType enum names to our tracked block names
        switch (typeName) {
            case "FURNACE": return "FURNACE";
            case "CHEST": return "CHEST";
            case "TRAPPED_CHEST": return "TRAPPED_CHEST";
            case "ENDER_CHEST": return "ENDER_CHEST";
            case "DISPENSER": return "DISPENSER";
            case "DROPPER": return "DROPPER";
            case "HOPPER": return "HOPPER";
            case "BREWING_STAND": return "BREWING_STAND";
            case "ENCHANTING_TABLE": return "ENCHANTING_TABLE";
            case "BEACON": return "BEACON";
            case "BARREL": return "BARREL";
            case "BLAST_FURNACE": return "BLAST_FURNACE";
            case "SMOKER": return "SMOKER";
            case "SHULKER_BOX": return "SHULKER_BOX";
            case "WHITE_SHULKER_BOX": return "WHITE_SHULKER_BOX";
            case "ORANGE_SHULKER_BOX": return "ORANGE_SHULKER_BOX";
            case "MAGENTA_SHULKER_BOX": return "MAGENTA_SHULKER_BOX";
            case "LIGHT_BLUE_SHULKER_BOX": return "LIGHT_BLUE_SHULKER_BOX";
            case "YELLOW_SHULKER_BOX": return "YELLOW_SHULKER_BOX";
            case "LIME_SHULKER_BOX": return "LIME_SHULKER_BOX";
            case "PINK_SHULKER_BOX": return "PINK_SHULKER_BOX";
            case "GRAY_SHULKER_BOX": return "GRAY_SHULKER_BOX";
            case "LIGHT_GRAY_SHULKER_BOX": return "LIGHT_GRAY_SHULKER_BOX";
            case "CYAN_SHULKER_BOX": return "CYAN_SHULKER_BOX";
            case "PURPLE_SHULKER_BOX": return "PURPLE_SHULKER_BOX";
            case "BLUE_SHULKER_BOX": return "BLUE_SHULKER_BOX";
            case "BROWN_SHULKER_BOX": return "BROWN_SHULKER_BOX";
            case "GREEN_SHULKER_BOX": return "GREEN_SHULKER_BOX";
            case "RED_SHULKER_BOX": return "RED_SHULKER_BOX";
            case "BLACK_SHULKER_BOX": return "BLACK_SHULKER_BOX";
            default: return null;
        }
    }
    
    
    private static class ChunkPos {
        final int x;
        final int z;
        
        ChunkPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPos chunkPos = (ChunkPos) o;
            return x == chunkPos.x && z == chunkPos.z;
        }
        
        @Override
        public int hashCode() {
            return 31 * x + z;
        }
    }
}
