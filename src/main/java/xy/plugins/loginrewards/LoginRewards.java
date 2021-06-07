package xy.plugins.loginrewards;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoginRewards extends JavaPlugin implements Listener {

    public static ItemStack todayBlock;

    public static String guititle;

    public static String mainPath;

    public static int user_days;

    public static String stringid;

    public static String current_day;

    private int on;

    public String alert;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        mainPath = getDataFolder().getPath() + "/";
        loadSettings();
        getServer().getPluginManager().registerEvents(this, (Plugin)this);
        getServer().getPluginManager().registerEvents(new MenuHandler(), (Plugin)this);
        System.out.println("LoginRewards is running");
    }

    public void loadSettings() {
        System.out.println("Started loadSettings");
        File config = new File(mainPath, "config.yml");
        YamlConfiguration yamlConfiguration1 = YamlConfiguration.loadConfiguration(config);
        File file = new File(mainPath, "players.yml");
        YamlConfiguration yamlConfiguration2 = YamlConfiguration.loadConfiguration(file);
        Date serverday = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String current_day = format.format(serverday);
        this.on = yamlConfiguration1.getInt("loginauto");
        this.alert = yamlConfiguration1.getString("alert");
        guititle = yamlConfiguration1.getString("GUITitle");
        if (!file.exists()) {
            yamlConfiguration2.addDefault("today.date", current_day);
            yamlConfiguration2.addDefault("yesterday.date", "20200505");
            yamlConfiguration2.options().copyDefaults(true);
            try {
                yamlConfiguration2.save(file);
            } catch (IOException iOException) {}
        }
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("loginreward")) {
            if (!(sender instanceof Player)) {
                System.out.println("Cannot claim login rewards from console.");
                return true;
            }

            Player p = (Player)sender;
            checkData(p);
            return true;
        }
        if (command.getName().equals("loginreload")) {
            if (sender instanceof Player) {
                Player p = (Player)sender;
                if (p.hasPermission("Login.reload")) {
                    p.sendMessage("Plugin has been reloaded");
                    loadSettings();
                } else {
                    p.sendMessage("No permission");
                }
            } else {
                System.out.println("Plugin has been reloaded");
                loadSettings();
            }
            return true;
        }
        return false;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        File file = new File(mainPath, "players.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        UUID uuid = p.getUniqueId();
        stringid = uuid.toString();
        Date serverday = new Date();
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd");
        current_day = format.format(serverday);

        if (!yamlConfiguration.contains(stringid)) {
            yamlConfiguration.addDefault(stringid + ".days", Integer.valueOf(0));
            yamlConfiguration.addDefault(stringid + ".date", yamlConfiguration.get("yesterday.date"));
            yamlConfiguration.options().copyDefaults(true);
            try {
                yamlConfiguration.save(file);
            } catch (IOException iOException) {}
        }
        if (!yamlConfiguration.get(stringid + ".date").equals(current_day)) {
            if (this.on == 1) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> checkData(p), 20);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> p.sendMessage(alert), 20);
            }

        }
    }

    public void checkData(Player p) {
        File file = new File(mainPath, "players.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        UUID uuid = p.getUniqueId();
        stringid = uuid.toString();
        String stored_day = (String)yamlConfiguration.get("today.date");
        Date serverday = new Date();
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd");
        current_day = format.format(serverday);

        if (!stored_day.equals(current_day)) {
            yamlConfiguration.set("yesterday.date", stored_day);
            yamlConfiguration.set("today.date", current_day);
            try {
                yamlConfiguration.save(file);
            } catch (IOException iOException) {}
        }
        if (!yamlConfiguration.contains(stringid)) {
            yamlConfiguration.addDefault(stringid + ".days", Integer.valueOf(0));
            yamlConfiguration.addDefault(stringid + ".date", yamlConfiguration.get("yesterday.date"));
            yamlConfiguration.options().copyDefaults(true);
            try {
                yamlConfiguration.save(file);
            } catch (IOException iOException) {}
        }
        String user_day = (String)yamlConfiguration.get(stringid + ".date");
        String yesterday = (String)yamlConfiguration.get("yesterday.date");
        user_days = yamlConfiguration.getInt(stringid + ".days");
        if (user_days == 28)
            user_days = 0;
        int claim_ready = 0;
        if (!user_day.equals(current_day)) {
            claim_ready = 1;
            user_days++;
            if (!user_day.equals(yesterday))
                user_days = 1;
        }

        openGUI(p, claim_ready);
    }

    public void openGUI(Player p, int claim_ready) {
        File config = new File(mainPath, "config.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(config);
        Inventory gui = Bukkit.createInventory((InventoryHolder)p, 27, guititle);
        gui.setMaxStackSize(1);
        ItemStack unclaimeddays = new ItemStack(Material.COAL_BLOCK, 1);
        ItemMeta metaDays = unclaimeddays.getItemMeta();
        ItemStack highlight = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
        ItemStack border = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
        ItemMeta border_meta = border.getItemMeta();
        border_meta.setDisplayName("§4Daily Rewards");
        Calendar c = Calendar.getInstance();
        c.add(5, 1);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        long minstillMidnight = (c.getTimeInMillis() - System.currentTimeMillis()) / 60000L;
        int hourstilMidnight = Math.round((float)(minstillMidnight / 60L));
        minstillMidnight -= (hourstilMidnight * 60);
        List<String> loreBorder = yamlConfiguration.getStringList("Border.lore");
        loreBorder.add("§4" + hourstilMidnight + "h " + minstillMidnight + "m");
        border_meta.setLore(loreBorder);
        border.setItemMeta(border_meta);
        if (claim_ready == 1) {
            border_meta.addEnchant(Enchantment.ARROW_INFINITE, 0, true);
            border_meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
        }
        highlight.setItemMeta(border_meta);
        ItemMeta todayMeta = unclaimeddays.getItemMeta();
        ArrayList<String> loreToday = new ArrayList<>();
        todayBlock = new ItemStack(Material.EMERALD_BLOCK, 1);
        if (claim_ready == 1) {
            todayBlock = new ItemStack(Material.EMERALD_BLOCK, 1);
            todayMeta.setUnbreakable(true);
            todayMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_UNBREAKABLE });
            loreToday.add(yamlConfiguration.getString("CurrentDay.claimable"));
        } else {
            todayBlock = new ItemStack(Material.DIAMOND_BLOCK, 1);
            loreToday.add(yamlConfiguration.getString("CurrentDay.claimed"));
        }
        loreToday.add(" ");
        loreToday.add(yamlConfiguration.getString("CurrentDay.lore"));
        for (int x = 0; x <= 19; x++) {
            int index = x;
            if (index >= 10)
                index = x + 7;
            gui.setItem(index, border);
        }
        int balance = -9;
        int weekdays = user_days;
        if (weekdays >= 8) {
            balance = -2;
            if (weekdays >= 15) {
                balance = 5;
                if (weekdays >= 22) {
                    balance = 12;
                    if (weekdays >= 29) {
                        weekdays = 1;
                        user_days = 1;
                    }
                    weekdays -= 7;
                }
                weekdays -= 7;
            }
            weekdays -= 7;
        }
        for (int y = 10; y <= 16; y++) {
            int pos = y + balance;
            List<String> dayRewards = yamlConfiguration.getStringList("Rewards.Day." + pos + ".text");
            int i = 0;
            metaDays.setDisplayName("§4Day " + (y + balance));
            List<String> loreDays = yamlConfiguration.getStringList("DayTemplate.lore");
            if (y + balance == user_days) {
                while (i < dayRewards.size()) {
                    loreToday.add(dayRewards.get(i));
                    i++;
                }
                todayMeta.setDisplayName("§4Day " + (y + balance));
                todayMeta.setLore(loreToday);
                todayBlock.setItemMeta(todayMeta);
                gui.setItem(y, todayBlock);
            } else {
                while (i < dayRewards.size()) {
                    loreDays.add(dayRewards.get(i));
                    i++;
                }
                metaDays.setDisplayName("§4Day " + (y + balance));
                metaDays.setLore(loreDays);
                unclaimeddays.setItemMeta(metaDays);
                gui.setItem(y, unclaimeddays);
                loreDays.remove(loreDays.size() - 1);
                loreDays.remove(loreDays.size() - 1);
            }
        }
        gui.setItem(weekdays, highlight);
        gui.setItem(weekdays + 18, highlight);
        p.openInventory(gui);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
