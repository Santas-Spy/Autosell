package santas.spy.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Chest;

import net.md_5.bungee.api.ChatColor;

import santas.spy.AutoSell;
import santas.spy.chests.ChestList;

public class ChestBreakListener implements Listener
{
    AutoSell plugin = AutoSell.getPlugin();
    ChestList chestList = AutoSell.getChestList();
    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            plugin.getLogger().info("Chest Broken!");
            Chest chest = (Chest)event.getBlock().getState();
            if (chest.getCustomName() != null) {
                if (chest.getCustomName().equals(ChatColor.YELLOW + "AutoSell Chest")) {
                    ItemStack items = new ItemStack(Material.CHEST);
                    ItemMeta meta = items.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW + "AutoSell Chest");
                    items.setItemMeta(meta);
                    event.getPlayer().getInventory().addItem(items);

                    chestList.remove(event.getBlock().getLocation());
                }
            }
        }
    }       
}
