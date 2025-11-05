package com.zenith.blockesp;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;
import com.zenith.discord.Embed;

import java.util.Set;
import java.util.stream.Collectors;

import static com.zenith.Globals.MODULE;

public class BlockESPCommand extends Command {
    
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("blockesp")
            .category(CommandCategory.MODULE)
            .description("Block ESP for base detection")
            .usageLines(
                "on/off",
                "add <block>",
                "remove <block>",
                "list",
                "trigger <amount>",
                "ownerping on/off"
            )
            .build();
    }
    
    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("blockesp")
            .then(literal("on").executes(c -> {
                BlockESPPlugin.PLUGIN_CONFIG.setEnabled(true);
                MODULE.get(BlockESPModule.class).syncEnabledFromConfig();
                c.getSource().getEmbed()
                    .title("BlockESP Enabled");
                return 1;
            }))
            .then(literal("off").executes(c -> {
                BlockESPPlugin.PLUGIN_CONFIG.setEnabled(false);
                MODULE.get(BlockESPModule.class).syncEnabledFromConfig();
                c.getSource().getEmbed()
                    .title("BlockESP Disabled");
                return 1;
            }))
            .then(literal("add")
                .then(argument("block", StringArgumentType.greedyString()).executes(c -> {
                    String blockName = StringArgumentType.getString(c, "block").toUpperCase().replace(" ", "_");
                    BlockESPPlugin.PLUGIN_CONFIG.addTrackedBlock(blockName);
                    c.getSource().getEmbed()
                        .title("Block Added: " + formatBlockName(blockName));
                    return 1;
                })))
            .then(literal("remove")
                .then(argument("block", StringArgumentType.greedyString()).executes(c -> {
                    String blockName = StringArgumentType.getString(c, "block").toUpperCase().replace(" ", "_");
                    if (BlockESPPlugin.PLUGIN_CONFIG.removeTrackedBlock(blockName)) {
                        c.getSource().getEmbed()
                            .title("Block Removed: " + formatBlockName(blockName));
                    } else {
                        c.getSource().getEmbed()
                            .title("Block Not Found")
                            .errorColor();
                    }
                    return 1;
                })))
            .then(literal("list").executes(c -> {
                Set<String> blocks = BlockESPPlugin.PLUGIN_CONFIG.getTrackedBlocks();
                String blockList = blocks.stream()
                    .map(this::formatBlockName)
                    .sorted()
                    .collect(Collectors.joining(", "));
                c.getSource().getEmbed()
                    .title("Tracked Blocks (" + blocks.size() + ")")
                    .description(blockList.isEmpty() ? "None" : blockList);
                return 1;
            }))
            .then(literal("trigger")
                .then(argument("amount", IntegerArgumentType.integer(1, 1000)).executes(c -> {
                    int amount = IntegerArgumentType.getInteger(c, "amount");
                    BlockESPPlugin.PLUGIN_CONFIG.setTriggerAmount(amount);
                    c.getSource().getEmbed()
                        .title("Trigger Set: " + amount);
                    return 1;
                })))
            .then(literal("ownerping")
                .then(literal("on").executes(c -> {
                    BlockESPPlugin.PLUGIN_CONFIG.setOwnerPingEnabled(true);
                    c.getSource().getEmbed()
                        .title("Owner Ping Enabled")
                        .description("Discord alerts will ping the account owner role");
                    return 1;
                }))
                .then(literal("off").executes(c -> {
                    BlockESPPlugin.PLUGIN_CONFIG.setOwnerPingEnabled(false);
                    c.getSource().getEmbed()
                        .title("Owner Ping Disabled")
                        .description("Discord alerts will not ping the account owner role");
                    return 1;
                })));
    }
    
    @Override
    public void defaultEmbed(Embed embed) {
        BlockESPConfig config = BlockESPPlugin.PLUGIN_CONFIG;
        embed
            .primaryColor()
            .addField("Enabled", config.isEnabled() ? "Yes" : "No")
            .addField("Trigger Amount", String.valueOf(config.getTriggerAmount()))
            .addField("Tracked Blocks", String.valueOf(config.getTrackedBlocks().size()))
            .addField("Owner Ping", config.isOwnerPingEnabled() ? "Enabled" : "Disabled");
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
}
