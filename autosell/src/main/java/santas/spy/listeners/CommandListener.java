package santas.spy.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import santas.spy.AutoSell;
import santas.spy.chests.ChestList;

public class CommandListener implements CommandExecutor
{
    AutoSell plugin = AutoSell.getPlugin();
    ChestList chestList = AutoSell.getChestList();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
        {
            Player player = (Player)sender;
            switch (args[0].toLowerCase()) {
                case "sell":
                    try {
                        chestList.sellAll(player);
                        plugin.getLogger().info(ChatColor.GREEN + "Selling All Chest Contents");
                    } catch (Exception e) {
                        plugin.getLogger().info("There were no autosell chests. " + e);
                    }   
                    break;

                case "give":
                    ItemStack chest = new ItemStack(Material.CHEST);
                    ItemMeta meta = chest.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW + "AutoSell Chest");
                    chest.setItemMeta(meta);
                    player.getInventory().addItem(chest);
                    plugin.getLogger().info(ChatColor.GREEN + "Giving autosell chest");
                    break;

                case "list":
                    chestList.listAll(player);
                    break;

                case "reload":
                    plugin.getConfig();
                    for (int i = 0; i < chestList.size(); i++)
                    {
                        chestList.getChest(i).reloadTimer();
                    }
                    player.sendMessage("[AutoSell] Reloaded Config!");
                    break;
                
                case "read":
                    serializeItem(player.getInventory().getItemInMainHand());
                    player.sendMessage("[AutoSell] Serialized item. look in testItem.txt");
                    giveItem(player);
                    break;

                default:
                    player.sendMessage("Unknown Command");
                    break;
            }
        }
        return true;
    }
    
    //REMOVE ME
    /*
    public void serializeItem (ItemStack item)
    {
        plugin.getLogger().info("Attempting to save item " + item.getType().toString());
        try {
            FileConfiguration data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "testItem.txt"));
            ConfigurationSection cs = data.createSection("testItem");
            cs.set("testItem",item.serialize());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save item! " + e);
        }
    }*/

    public void serializeItem (ItemStack item)
    {
        plugin.getLogger().info("Attempting to save item " + item.getType().toString());
        try {
            File file = new File(plugin.getDataFolder(), "testItem.yml");

            if (file.createNewFile())
            {
                plugin.getLogger().info("Making a new testItem.yml");
            }
            plugin.getLogger().info(file.getAbsolutePath());

            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection cs = data.createSection("createSection");
            cs.set("testSet",item);
            data.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save item! " + e);
        }
    }

    public void giveItem (Player player)
    {
        plugin.getLogger().info("Attempting to give item");
        try {
            File file = new File(plugin.getDataFolder(), "testItem.yml");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            data.load(file);
            ConfigurationSection cs = data.getConfigurationSection("createSection");
            ItemStack item = cs.getItemStack("testSet");
            plugin.getLogger().info(item.getType().toString() + ":" + item.getAmount());
            player.getInventory().addItem(item);
        } catch (Exception e) {
            plugin.getLogger().warning("[Error]. could not do it lol " + e);
        }
    }
}
