package com.zenith.blockesp;

import com.zenith.plugin.api.Plugin;
import com.zenith.plugin.api.PluginAPI;
import com.zenith.plugin.api.ZenithProxyPlugin;

@Plugin(
    id = "blockesp",
    version = "1.0.0",
    description = "ESP for detecting potential bases by tracking specific blocks",
    url = "https://github.com/zenithproxy/blockesp-plugin",
    authors = {"BlockESP"},
    mcVersions = {"1.21.4"}
)
public class BlockESPPlugin implements ZenithProxyPlugin {
    public static BlockESPConfig PLUGIN_CONFIG;
    
    @Override
    public void onLoad(PluginAPI api) {
        PLUGIN_CONFIG = api.registerConfig("blockesp", BlockESPConfig.class);
        api.registerModule(new BlockESPModule());
        api.registerCommand(new BlockESPCommand());
    }
}
