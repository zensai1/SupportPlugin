package io.github.zensai1.supportPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Nickname implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        if (args.length != 1) {
            sender.sendMessage("§c使い方: /nickname <名前>");
            return true;
        }

        String nickname = args[0];

        // storage に書き込むコマンドを実行
        String cmd = String.format(
                "data modify storage jinro_rpg: Pl.Nickname set value \"%s\"",
                nickname
        );

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        return true;
    }
}
