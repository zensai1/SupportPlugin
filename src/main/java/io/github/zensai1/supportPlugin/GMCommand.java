package io.github.zensai1.supportPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GMCommand  implements CommandExecutor {
    private static final String OBJECTIVE_NAME = "Znsi.Plugin";
    private static final String DUMMY_PLAYER = "***";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        if (args.length != 1) {
            sender.sendMessage("引数を1つ以上入力してください");
            return false;
        }

        //startcommand
        if (Objects.equals(args[0] , "start")) {
            int score;
            try {
                score = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("MAPは数字で入力してください");
                return true;
            }

            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

            Objective obj = board.getObjective(OBJECTIVE_NAME);
            obj.getScore(DUMMY_PLAYER).setScore(score);
            return true;
        }
        return false;
    }
}
