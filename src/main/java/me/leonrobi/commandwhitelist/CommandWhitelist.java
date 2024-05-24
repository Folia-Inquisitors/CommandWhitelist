package me.leonrobi.commandwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class CommandWhitelist extends JavaPlugin implements Listener {

    private FileConfiguration config = this.getConfig();

    public static CommandWhitelist INSTANCE;

    //private static final HashSet<String> allowedOps = new HashSet<>();
    private static final HashMap<String, HashSet<String>> whitelistedCommands = new HashMap<>();
    private static final HashMap<String, HashSet<String>> blacklistCommands = new HashMap<>();

    @Override
    public void onEnable() {
        INSTANCE = this;

        config.addDefault("unknown-cmd-msg", "Unknown command. Type \"/help\" for help.");

        //config.addDefault("operator-shield.enabled", true);
        //config.addDefault("operator-shield.allowed-ops", List.of("leonrobi"));

        config.addDefault("groups.default.hide-all", List.of("tpa", "msg"));
        config.addDefault("groups.default.add-all", List.of("tpa"));
        config.addDefault("groups.admin.hide-all", List.of("plugins", "vanish"));

        config.options().copyDefaults(true);
        this.saveConfig();

        getCommand("cwreload").setExecutor(new CWReloadCommand());
        getServer().getPluginManager().registerEvents(this, this);

        reloadConfigPls();
    }

    public void reloadConfigPls() {
            config = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/config.yml"));

        //allowedOps.clear();
        //allowedOps.addAll(config.getStringList("operator-shield.allowed-ops"));

        whitelistedCommands.clear();
        blacklistCommands.clear();

        for (String group : Objects.requireNonNull(config.getConfigurationSection("groups"))
                .getKeys(false)) {
            whitelistedCommands.put(group,
                    new HashSet<>(config.getStringList("groups." + group + ".hide-all")));
            blacklistCommands.put(group,
                    new HashSet<>(config.getStringList("groups." + group + ".add-all")));
        }

//        for (String name : whitelistedCommands.keySet()) {
//            String value = whitelistedCommands.get(name).toString();
//            Bukkit.broadcastMessage(name + " " + value);
//        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.updateCommands();
        }
    }

    public HashSet<String> findGroups(Player player) {
        HashSet<String> groups = new HashSet<>();

        for (String group : whitelistedCommands.keySet()) {
            if (player.hasPermission("commandwhitelist." + group)) {
                groups.add(group);
            }
        }

        return groups;
    }

    private boolean hasBypass(Player player) {
        return player.isOp() || player.hasPermission("commandwhitelist.bypass");
    }

    @EventHandler
    public void onCommandTab(PlayerCommandSendEvent event) {
        if (hasBypass(event.getPlayer())) {
            return;
        }

        HashSet<String> groups = findGroups(event.getPlayer());
        HashSet<String> commands = new HashSet<>();

        for (String group : groups) {
            commands.addAll(whitelistedCommands.get(group));
        }
        for (String group : groups) {
            commands.removeAll(blacklistCommands.get(group));
        }

        event.getCommands().removeIf(command -> !commands.contains(command));
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (hasBypass(player)) {
            return;
        }

        HashSet<String> groups = findGroups(player);
        HashSet<String> commands = new HashSet<>();

        for (String group : groups) {
            commands.addAll(whitelistedCommands.get(group));
        }
        for (String group : groups) {
            commands.removeAll(blacklistCommands.get(group));
        }

        String cmd = event.getMessage().split(" ")[0].substring(1);

        if (!commands.isEmpty()) {
            if (!commands.contains(cmd)) {
                event.setCancelled(true);
                player.sendMessage(config.getString("unknown-cmd-msg"));
            }
        } else {
            if (!groups.isEmpty()) {
                getLogger().severe("No whitelisted commands for groups '" + groups + "'");
            }
        }
    }

}
