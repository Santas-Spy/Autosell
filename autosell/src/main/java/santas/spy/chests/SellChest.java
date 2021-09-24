package santas.spy.chests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.inventory.Inventory;

import net.milkbowl.vault.economy.Economy;

import net.md_5.bungee.api.ChatColor;

import santas.spy.AutoSell;

public class SellChest
{
    AutoSell plugin = AutoSell.getPlugin();
    Economy econ = plugin.getEconomy();
    private int taskID;
    private OfflinePlayer owner;
    private Location location;
    private int timer;  //Ticks between sell chest selling contents

    public SellChest(OfflinePlayer newOwner, Location newLocation)
    {
        owner = newOwner;
        location = newLocation;
        timer =  Integer.parseInt(plugin.getConfig().getString("chest-timer"));
        taskID = timer();
    }

    public void setPlayer(OfflinePlayer newOwner)
    {
        owner = newOwner;
    }

    public OfflinePlayer getPlayer()
    {
        return owner;
    }

    public void setLocation(Location newLocation)
    {
        location = newLocation;
    }

    public Location getLocation()
    {
        return location;
    }

    public int sellContents()
    {
        Chest chest = null;
        int total = 0;
        try {
            chest = (Chest)location.getBlock().getState();
        } catch (ClassCastException e) {
            plugin.getLogger().warning(ChatColor.RED + "[ERROR] chest.txt contains a chest that doesn't exist!!!");
            plugin.getLogger().warning(ChatColor.RED + "[ERROR] please fix by either placing an autosell chest at " + info());
            plugin.getLogger().warning(ChatColor.RED + "[ERROR] or removing " + info() + " from chest.txt");
            plugin.getLogger().warning(ChatColor.RED + "[ERROR] this was likely caused by a server crash or a bug. Please contact the dev if you belive this is a bug");
        }

        if (chest != null) {
            Inventory contents = chest.getBlockInventory();
            ItemStack[] items = contents.getContents();
            File prices =  null;
            try {
                prices = new File(Bukkit.getServer().getPluginManager().getPlugin("AutoSell").getDataFolder(),"prices.yml");
            } catch (Exception e) {
                plugin.getLogger().warning("ERROR: prices.yml missing! " + e);
            }

            for (int i = 0; i < items.length; i++) {                                                            //loop for the contents in the chest
                try {
                    if (items[i] != null) {                                                                     //If there is an item in this slot
                        if (plugin.debugLevel() > 0)
                            plugin.getLogger().info("Chest Contents: " + items[i].getType().toString());        //Print contents of the chest 
                        //to anyone reading this, yes its a mess but at this point I'm so over it
                        try {                       
                            double value = getValue(items[i], prices)*items[i].getAmount();                     //get the value and multiply by the number of items in this stack
                            contents.remove(items[i]);                                                          //remove these items from the chest
                            if (plugin.debugLevel() > 0)
                                plugin.getLogger().info(ChatColor.GREEN + "Depositing " + value + " into " + owner.getName() + "'s account.");
                            econ.depositPlayer(owner, value);                                                   //pay the user
                            total += value;

                        } catch (FileNotFoundException e) {                                                     //thrown by getValue()
                            if (plugin.debugLevel() > 1)
                                plugin.getLogger().warning("Could not find " + items[i].getType().toString() + " in prices.yml");

                        } catch (NullPointerException e) {                                                      //if prices.yml was not found it was initialised to null
                            plugin.getLogger().warning("NullPointerException thrown due to prices.yml pointer being null. Probable cause: prices.yml is missing");
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to sell item in slot " + i +" in chest " + info());
                    if (plugin.debugLevel() > 1) 
                        plugin.getLogger().warning("Fail due to exception: " + e);
                }
            }
        } else {
            total = 0;
        }
        return total;
    }

    public double getValue(ItemStack item, File prices) throws FileNotFoundException
    {
        double price = 0;
        boolean found = false;
        String line = "";

        FileReader reader;
        BufferedReader buffer = null;

        try 
        {
            reader = new FileReader(prices);
            buffer = new BufferedReader(reader);
            if (plugin.debugLevel() > 1)
                plugin.getLogger().info("Looking for " + item.getType().toString() + " in prices.yml");

            line = buffer.readLine();

            while (!found && line != null) {        //While we havent found the price and not EOF
                String[] data = line.split(":");    //Split the line
                if (data[0].toLowerCase().equals(item.getType().toString().toLowerCase())) {    //If the name matches the material type in the chest
                    if (plugin.debugLevel() > 1)
                        plugin.getLogger().info("Found " + item.getType().toString().toLowerCase() + " in prices.yml");  
                    found = true;

                    try {
                        price = Double.parseDouble(data[1]); //Set the price
                        if (plugin.debugLevel() > 1)
                            plugin.getLogger().info("Found price of " + item.getType().toString().toLowerCase() + " in prices.yml");
                    } catch (NullPointerException e) {
                        if (plugin.debugLevel() > 0)
                            plugin.getLogger().warning(ChatColor.RED + "[ERROR] missing price in prices.yml at item " + data[0]);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning(ChatColor.RED + "[ERROR] invalid price in prices.yml at item " + data[0]);
                    }
                }
                line = buffer.readLine();   //Read next line
            }
            buffer.close();
        } catch (FileNotFoundException e) {
            plugin.getLogger().warning(ChatColor.RED + "[ERROR] could not find prices.yml");
        } catch (Exception e) {
            plugin.getLogger().info(ChatColor.RED + "[ERROR] exception at SellChest.getValue() " + e);
        }
        if (!found && line == null) {   //Item was not found in prices
            throw new FileNotFoundException();
        }
        return price;
    }

    public String info()
    {
        return ("Owner: " + owner.getName() + ", Location: " + location.getWorld().getName() + " " + (int)location.getX() + " / " + (int)location.getY() + " / " + (int)location.getZ());
    }

    private int timer()
    {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        int taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable(){
            @Override
            public void run()
            {
                sellContents();
            }
        }, timer, timer);

        return taskID;
    }

    public void close()
    {
        plugin.getServer().getScheduler().cancelTask(taskID);
    }

    public void reloadTimer()
    {
        timer =  Integer.parseInt(plugin.getConfig().getString("chest-timer"));
    }
}
