package io.github.zensai1.supportPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GMCommand  implements CommandExecutor {
    private static final String OBJECTIVE_NAME = "Znsi.Plugin";
    private static final String DUMMY_PLAYER = "***";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§c使い方: /jr <start|stop> [値]");
            return false;
        }

        // プレイヤー以外（コンソール）を弾く
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます");
            return true;
        }

        // Tag チェック
        if (!player.getScoreboardTags().contains("op")) {
            player.sendMessage("§cこのコマンドを使う権限がありません");
            return true;
        }


        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective(OBJECTIVE_NAME);
        String name = sender.getName();

        //startcommand
        if (args[0].equalsIgnoreCase("start")) {

            if (args.length != 2) {
                sender.sendMessage("§c使い方: /jr start <数字>");
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c数字を指定してください");
                return true;
            }

            obj.getScore(DUMMY_PLAYER).setScore(value);
            sender.sendMessage("§aZnsi.Plugin を " + value + " に設定しました");
            return true;
        }

        // /jr stop
        if (args[0].equalsIgnoreCase("stop")) {

            // ★ 実行させたいコマンド（/ は付けない）
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "execute as " + name + " run say /jr stopを実行したよ");

            return true;
        }
        return false;
    }
}
