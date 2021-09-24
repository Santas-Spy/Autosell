package santas.spy;

/*To Do

* Make each chest only open prices.yml once
* more dynamic prices.yml with more info
    * based off BSP for parity
* Autosell tag should be in NBT
    * Autosell chests should be renameable
* make /reload chests incase of class cast exception in SellChest.sellContents()?

*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import santas.spy.chests.ChestList;
import santas.spy.chests.SellChest;
import santas.spy.listeners.ChestPlaceListener;
import santas.spy.listeners.ChestBreakListener;
import santas.spy.listeners.CommandListener;

import net.md_5.bungee.api.ChatColor;

public class AutoSell extends JavaPlugin {
    Server server = Bukkit.getServer();
    ConsoleCommandSender console = server.getConsoleSender();

    private static AutoSell plugin;
    private static MultiverseCore core;
    private static ChestList chestList;
    private static MVWorldManager worldManager;
    private Economy econ;
    private Permission perms;
    private Chat chat;
    int debugLevel;

    @Override
    public void onEnable() {
        //Setup plugin instance
        plugin = this;
        getLogger().info("Autosell Starting!");

        //Setup config
        plugin.saveDefaultConfig();
        readConfig();
        createCoreFiles();

        //Setup dependancies
        setupMultiverse();
        setupVault();
        
        //Load Chests 
        chestList = loadChests();

        //Get listeners (Must do this last)
        registerListeners();
        getLogger().info("Chests Loaded!");
        getLogger().info("AutoSell Loaded!");

    }
    @Override
    public void onDisable() {
        chestList.saveChests();
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        getLogger().info("Autosell Disabled!");
        plugin = null;
    }

    public void createCoreFiles()
    {
        //Create Data File
        if (!getDataFolder().exists()) {
            if (debugLevel > 1) 
                console.sendMessage(ChatColor.YELLOW + "MAKING DATA FOLDER");

            //If not found, make the data file
            getDataFolder().mkdir();
        } else {
            if (debugLevel > 1) 
                console.sendMessage(ChatColor.GREEN + "Found files");
        }

        //Create config.yml
        File config = new File(getDataFolder(),"config.yml");
        if (config.exists()) {
            if (debugLevel > 1)
                console.sendMessage(ChatColor.GREEN + "found config");
        } else {
            try {
                if (config.createNewFile()) {
                    if (debugLevel > 1)
                        console.sendMessage(ChatColor.GREEN + "config.yml was created");
                } else {
                    console.sendMessage(ChatColor.RED + "[FAIL!] config.yml was not created!");
                }
            } catch (Exception e) {
                console.sendMessage(ChatColor.RED + "[ERROR!] config.yml was not created due to exception " + e);
            }
        }

        //Create prices.yml
        File dataFolder = getDataFolder();
        File prices = new File(dataFolder,"prices.yml");
        if (prices.exists()) {
            if (debugLevel > 1)
                console.sendMessage(ChatColor.GREEN + "found prices");
        } else {
            if (debugLevel > 1)
                console.sendMessage(ChatColor.YELLOW + "[Warn!] prices.yml Does not exist. creating now!");
            try {
                if (prices.createNewFile()) {
                    if (debugLevel > 1)
                        console.sendMessage(ChatColor.GREEN + "[Success!] prices.yml was created!");
                } else {
                    console.sendMessage(ChatColor.RED + "[FAIL!] prices.yml was not created!");
                }
            } catch (Exception e) {
                console.sendMessage(ChatColor.RED + "[ERROR!] prices.yml was not created due to exception " + e);
            }
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    //Accessors
    public Economy getEconomy() {
        return econ;
    }

    public Permission getPermissions() {
        return perms;
    }

    public Chat getChat() {
        return chat;
    }
    
    public static AutoSell getPlugin()
    {
        return plugin;
    }

    public static ChestList getChestList()
    {
        return chestList;
    }

    public static MultiverseCore getMVCore()
    {
        return core;
    }

    public int debugLevel()
    {
        return debugLevel;
    }

    private void registerListeners()
    {
        this.getCommand("autosell").setExecutor(new CommandListener());
        getServer().getPluginManager().registerEvents(new ChestPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new ChestBreakListener(), this);
    }

    private void setupVault()
    {
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
    }

    private void setupMultiverse()
    {
        core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        worldManager = core.getMVWorldManager();
    }

    private ChestList loadChests()
    {
        ChestList list = new ChestList();
        FileReader file = null;
        BufferedReader reader = null;
        String line = "";

        if (debugLevel > 1)
            this.getLogger().info("Attempting to load chests");
        try {
            file = new FileReader(this.getDataFolder() + "/chests.txt");
            reader = new BufferedReader(file);
        } catch (Exception e) {
            this.getLogger().warning("ERROR: Could not find chests.txt");
        }

        try {
            line = reader.readLine();
            while(line != null) {
                try {
                    String[] data = line.split(",");
                    //Find world
                    MultiverseWorld multiverseWorld = worldManager.getMVWorld(data[1]);
                    World world = multiverseWorld.getCBWorld();

                    //Set the location of the chest
                    Location location = null;
                    try {
                        location = new Location(world,Integer.parseInt(data[2]),Integer.parseInt(data[3]),Integer.parseInt(data[4]));
                    } catch (NullPointerException e) {
                        getLogger().warning("ERROR!!! World object was null!");
                    }

                    //set the owner of the chest
                    OfflinePlayer owner = this.getServer().getOfflinePlayer(UUID.fromString(data[0]));

                    //add the chest to the list of chests
                    SellChest chest = new SellChest(owner,location);
                    list.add(chest);
                    if (debugLevel > 1) 
                        getLogger().info("Loaded " + chest.info());
                } catch (Exception e) {
                    this.getLogger().warning("Could not create chest! line: " + line + ". Exception: " + e);
                }
                line = reader.readLine();
            }
            reader.close();
            file.close();
        } catch (Exception e) {
            this.getLogger().warning("Could not read first line!");
        }

        return list;
    }

    private void readConfig()
    {
        getLogger().info("Running AutoSell at debug level: " + getConfig().getString("debug-level"));
        debugLevel = Integer.parseInt(getConfig().getString("debug-level"));
    }
}