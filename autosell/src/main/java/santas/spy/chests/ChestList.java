package santas.spy.chests;

import java.io.Serializable;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import santas.spy.AutoSell;

import net.md_5.bungee.api.ChatColor;

public class ChestList implements Serializable
{
    AutoSell plugin = AutoSell.getPlugin();
    private ArrayList<SellChest> chestList;


    public void listAll(Player player) {
        plugin.getLogger().info("List Size: " + chestList.size());
        if (chestList.size() > 0) {
            for (int i = 0; i < chestList.size(); i++) {
                player.sendMessage(chestList.get(i).info());
            }
        } else {
            player.sendMessage(ChatColor.RED + "There were no chests");
        }
    }

    public void add(SellChest chest)
    {
        chestList.add(chest);
    }

    public ChestList()
    {
        chestList = new ArrayList<SellChest>();
    }

    public int size()
    {
        return chestList.size();
    }

    public SellChest getChest(int pos)
    {
        return chestList.get(pos);
    }

    public void sellAll(Player player)
    {
        if (chestList.size() > 0) {
            for (int i = 0; i < chestList.size(); i++) {
                player.sendMessage("Sold contents for $" + chestList.get(i).sellContents());
            }
        } else {
            player.sendMessage("There are no chests");
        }
    }

    public void saveChests()
    {
        FileOutputStream file = null;
        PrintWriter pw = null;
        plugin.getLogger().info("Saving Chests!");

        //Open File
        try {
            file = new FileOutputStream(plugin.getDataFolder() + "/chests.txt");
            pw = new PrintWriter (file);
        } catch (Exception e) {
            plugin.getLogger().warning(ChatColor.RED + "[ERROR] could not save chests: could not write to file. " + e);
        }

        //Save Data
        for (int i = 0; i < chestList.size(); i++) {
            try {
                Location location = chestList.get(i).getLocation();
                pw.write(chestList.get(i).getPlayer().getUniqueId().toString() + "," + location.getWorld().getName() + "," +  location.getBlockX() + "," + location.getBlockY() + "," +  location.getBlockZ() + ", Owned By: " + chestList.get(i).getPlayer().getName() +"\n");
                plugin.getLogger().info("saved chest: " + chestList.get(i).getPlayer().getUniqueId().toString() + "," + location.getWorld().toString() + "," +  location.getBlockX() + "," + location.getBlockY() + "," +  location.getBlockZ());
            } catch (Exception e) {
                plugin.getLogger().warning("Could not save chest! " + e);
            }
        }

        //Close File
        try {
            pw.close();
            file.close();
        } catch (Exception e) {
            plugin.getLogger().info("Could not close chest save file! " + e);
        }
        plugin.getLogger().info("Saved Chests");
    }

    public void remove(Location location)
    {
        if (chestList.size() > 0) {
            boolean found = false;
            int i = 0;
            while (!found)
            {
                if (chestList.get(i).getLocation().equals(location)) {                                      //Search the list until we get a match
                    plugin.getLogger().info("Removed Chest! " + chestList.get(i).getLocation().toString()); //Inform that a match is found
                    chestList.get(i).close();                                                               //Close the chest object
                    chestList.remove(i);                                                                    //Remove the chest from the list
                    found = true;                                                                           //Stop looping
                }
                i++;                                                                                        //count what loop we are up to
            }
        }
    }
}   
