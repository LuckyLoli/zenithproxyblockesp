# ZenithProxy BlockESP Plugin 🚀

A ZenithProxy plugin that scans loaded chunks for blocks from a configurable list 🔍  
Once the minimum number of tracked blocks is detected, it automatically sends the coordinates and a summary to Discord 📩

**Example alert:**

Possible Base Found 🏠
X: 500 Z: -320

5 Ender Chests 📦
10 Chests 📦
7 Shulker Boxes 📦

---

## Commands ⚙️

| Command | Description |
|---|---|
| `.blockesp on/off` | Toggle the plugin on or off 🔛 |
| `.blockesp add <block>` | Add a block to the tracking list ➕ |
| `.blockesp remove <block>` | Remove a block from the tracking list ➖ |
| `.blockesp list` | Show all currently tracked blocks 📋 |
| `.blockesp trigger <amount>` | Set the minimum block count required to trigger an alert 🎯 |
| `.blockesp ownerping on/off` | Toggle pinging the `manage-proxy` role on alerts 🔔 |

---

## Tracked Blocks (Default List) 📦

<details>
<summary>Click to expand</summary>

- Barrel 🛢️  
- Beacon ✨  
- Black / Blue / Brown / Cyan / Gray / Green / Light Blue / Light Gray / Lime / Magenta / Orange / Pink / Purple / Red / White / Yellow Shulker Box 🎁  
- Comparator ⚖️  
- Crafting Table 🛠️  
- Dispenser 🎯  
- Dropper ⬇️  
- Enchanting Table 🔮  
- Ender Chest 📦  
- Glow Item Frame 🖼️  
- Hopper 🕳️  
- Item Frame 🖼️  
- Observer 👁️  
- Piston / Sticky Piston ⚙️  
- Redstone Torch / Redstone Wall Torch / Redstone Wire 🔴  
- Repeater 🔁  

</details>

---

## Installation 📥

1. Download the latest `.jar` from [Releases](../../releases) ⬇️  
2. Place it in your ZenithProxy `plugins/` folder 📂  
3. Restart ZenithProxy 🔄  

---

## Notes 📝

- Works with ZenithProxy on **1.21.4** ✅  
- Compatibility with `coordobf` is untested ⚠️  
- Use at your own risk — I am not responsible for bans or other consequences 🚫  
- This plugin is no longer actively maintained. Feel free to fork and modify it 🔧