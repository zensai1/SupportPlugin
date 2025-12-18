package io.github.zensai1.supportPlugin;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.GameMode;

import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class ScoreWatcher implements Listener {
    private final JavaPlugin plugin;
    private WebSocket ws;

    public ScoreWatcher(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // WebSocket接続
        ws = java.net.http.HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8080"), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        plugin.getLogger().info("Connected to WebSocket server");
                        WebSocket.Listener.super.onOpen(webSocket);
                    }
                }).join();

        // イベント登録
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // 定期タスクでスコアをチェック
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for(Player p : Bukkit.getOnlinePlayers()){
                int autoMute = p.getScoreboard().getObjective("Auto.Mute") == null ? 0 :
                        p.getScoreboard().getObjective("Auto.Mute").getScore(p.getName()).getScore();
                int moveCH = p.getScoreboard().getObjective("Znsi.MoveCH") == null ? 0 :
                        p.getScoreboard().getObjective("Znsi.MoveCH").getScore(p.getName()).getScore();

                JsonObject json = new JsonObject();
                json.addProperty("player", p.getName());
                json.addProperty("autoMute", autoMute);
                json.addProperty("moveCH", moveCH);

                ws.sendText(json.toString(), true);
            }
        }, 0L, 20L); // 1秒毎
    }

    public void stop() {
        if(ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "Bye");
    }

    // スペクテイターチャット
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        if(sender.getGameMode() != GameMode.SPECTATOR) return;
        event.setCancelled(true);
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.getGameMode() == GameMode.SPECTATOR){
                p.sendMessage("[Spec] " + sender.getName() + ": " + event.getMessage());
            }
        }
    }
}
