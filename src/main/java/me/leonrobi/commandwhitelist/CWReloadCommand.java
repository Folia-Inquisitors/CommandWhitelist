package me.leonrobi.commandwhitelist;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CWReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandWhitelist.INSTANCE.reloadConfigPls();
        sender.sendMessage(ChatColor.GREEN + "Reloaded config.yml");
        return false;
    }
}
