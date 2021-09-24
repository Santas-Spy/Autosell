package santas.spy.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.Listener;

import santas.spy.chests.SellChest;
import santas.spy.AutoSell;

public class ChestPlaceListener implements Listener
{
    AutoSell plugin = AutoSell.getPlugin();
    @EventHandler
    public void blockPlaceListener(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.CHEST) {
            if (event.getItemInHand().getItemMeta() != null) {
                if (event.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "AutoSell Chest")) {
                    if (event.getPlayer().hasPermission("autosell.create")) {
                        
                        plugin.getLogger().info(ChatColor.GREEN + event.getPlayer().getName() + " placed an autosell chest");
                        Player player = event.getPlayer();
                        Block block = event.getBlock();
                        Location location = block.getLocation();

                        SellChest newChest = new SellChest(player,location);
                        AutoSell.getChestList().add(newChest);
                    }
                }
            }
        }
    }
}
                    