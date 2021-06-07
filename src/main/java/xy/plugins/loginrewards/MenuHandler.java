package xy.plugins.loginrewards;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuHandler implements Listener {
    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(LoginRewards.guititle)) {
            Player p = (Player)e.getWhoClicked();
            if (e.getCurrentItem() == null)
                return;
            if (e.getCurrentItem().getItemMeta().isUnbreakable()) {
                if (p.getInventory().firstEmpty() != -1) {
                    giveRewards(p);
                } else {
                    p.sendMessage("Cannot claim rewards with a full inventory.");
                }
                e.setCancelled(true);
                p.closeInventory();
            } else if (e.getCurrentItem().getType().equals(Material.WHITE_STAINED_GLASS_PANE)) {
                e.setCancelled(true);
            } else if (e.getCurrentItem().getType().equals(Material.COAL_BLOCK)) {
                e.setCancelled(true);
            } else if (e.getCurrentItem().getType().equals(Material.DIAMOND_BLOCK)) {
                e.setCancelled(true);
            }
        }
    }

    public void giveRewards(Player p) {
        File config = new File(LoginRewards.mainPath, "config.yml");
        YamlConfiguration yamlConfiguration1 = YamlConfiguration.loadConfiguration(config);
        File file = new File(LoginRewards.mainPath, "players.yml");
        YamlConfiguration yamlConfiguration2 = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration2.set(LoginRewards.stringid + ".days", Integer.valueOf(LoginRewards.user_days));
        yamlConfiguration2.set(LoginRewards.stringid + ".date", LoginRewards.current_day);
        try {
            yamlConfiguration2.save(file);
        } catch (IOException iOException) {}
        for (String command : yamlConfiguration1.getStringList("Rewards.Day." + LoginRewards.user_days + ".commands")) {
            if (command.startsWith("[console] ")) {
                command = command.replace("[console] ", "");
                command = command.replace("<player>", p.getName());
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
            }
            if (command.startsWith("[player] ")) {
                command = command.replace("[player] ", "");
                command = command.replace("<player>", p.getName());
                p.performCommand(command);
            }
        }
    }
}
