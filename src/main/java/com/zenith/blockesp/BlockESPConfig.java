package com.zenith.blockesp;

import java.util.HashSet;
import java.util.Set;

public class BlockESPConfig {
    private boolean enabled = false;
    private int triggerAmount = 25;
    private Set<String> trackedBlocks = new HashSet<>();
    private boolean ownerPingEnabled = true; // Default to enabled
    
    public BlockESPConfig() {
        initializeDefaultBlocks();
    }
    
    private void initializeDefaultBlocks() {
        trackedBlocks.add("CRAFTING_TABLE");
        trackedBlocks.add("BARREL");
        trackedBlocks.add("ITEM_FRAME");
        trackedBlocks.add("GLOW_ITEM_FRAME");
        trackedBlocks.add("REDSTONE_WIRE");
        trackedBlocks.add("REDSTONE_TORCH");
        trackedBlocks.add("REDSTONE_WALL_TORCH");
        trackedBlocks.add("REPEATER");
        trackedBlocks.add("COMPARATOR");
        trackedBlocks.add("HOPPER");
        trackedBlocks.add("DISPENSER");
        trackedBlocks.add("DROPPER");
        trackedBlocks.add("PISTON");
        trackedBlocks.add("STICKY_PISTON");
        trackedBlocks.add("OBSERVER");
        trackedBlocks.add("SHULKER_BOX");
        trackedBlocks.add("WHITE_SHULKER_BOX");
        trackedBlocks.add("ORANGE_SHULKER_BOX");
        trackedBlocks.add("MAGENTA_SHULKER_BOX");
        trackedBlocks.add("LIGHT_BLUE_SHULKER_BOX");
        trackedBlocks.add("YELLOW_SHULKER_BOX");
        trackedBlocks.add("LIME_SHULKER_BOX");
        trackedBlocks.add("PINK_SHULKER_BOX");
        trackedBlocks.add("GRAY_SHULKER_BOX");
        trackedBlocks.add("LIGHT_GRAY_SHULKER_BOX");
        trackedBlocks.add("CYAN_SHULKER_BOX");
        trackedBlocks.add("PURPLE_SHULKER_BOX");
        trackedBlocks.add("BLUE_SHULKER_BOX");
        trackedBlocks.add("BROWN_SHULKER_BOX");
        trackedBlocks.add("GREEN_SHULKER_BOX");
        trackedBlocks.add("RED_SHULKER_BOX");
        trackedBlocks.add("BLACK_SHULKER_BOX");
        trackedBlocks.add("ENCHANTING_TABLE");
        trackedBlocks.add("ENDER_CHEST");
        trackedBlocks.add("CHEST");
        trackedBlocks.add("TRAPPED_CHEST");
        trackedBlocks.add("FURNACE");
        trackedBlocks.add("BLAST_FURNACE");
        trackedBlocks.add("SMOKER");
        trackedBlocks.add("BEACON");
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getTriggerAmount() {
        return triggerAmount;
    }
    
    public void setTriggerAmount(int triggerAmount) {
        this.triggerAmount = triggerAmount;
    }
    
    public Set<String> getTrackedBlocks() {
        return trackedBlocks;
    }
    
    public void setTrackedBlocks(Set<String> trackedBlocks) {
        this.trackedBlocks = trackedBlocks;
    }
    
    public void addTrackedBlock(String block) {
        trackedBlocks.add(block.toUpperCase());
    }
    
    public boolean removeTrackedBlock(String block) {
        return trackedBlocks.remove(block.toUpperCase());
    }
    
    public boolean isBlockTracked(String block) {
        return trackedBlocks.contains(block.toUpperCase());
    }
    
    public boolean isOwnerPingEnabled() {
        return ownerPingEnabled;
    }
    
    public void setOwnerPingEnabled(boolean ownerPingEnabled) {
        this.ownerPingEnabled = ownerPingEnabled;
    }
}
