package io.github.zensai1.supportPlugin;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SupportPlugin extends JavaPlugin implements Listener {

    private WebSocketClient wsClient;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final Map<String, Integer> lastAutoMute = new HashMap<>();
    private final Map<String, Integer> lastMoveCH = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("SupportPlugin enabled");

        // WebSocket接続
        try {
            wsClient = new WebSocketClient(new URI("ws://localhost:8080")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected.set(true);
                    getLogger().info("Connected to WebSocket server");
                }
                @Override
                public void onMessage(String message) {}
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected.set(false);
                    getLogger().warning("WebSocket closed: " + reason);
                }
                @Override
                public void onError(Exception ex) {
                    getLogger().warning("WebSocket error: " + ex.getMessage());
                }
            };
            wsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // イベント登録
        getServer().getPluginManager().registerEvents(this, this);

        // Tick チェックで補完（1 tick = 0.05秒）
        getServer().getScheduler().runTaskTimer(this, this::checkScores, 0L, 1L);
    }

    @Override
    public void onDisable() {
        getLogger().info("SupportPlugin disabled");
        if (wsClient != null) wsClient.close();
    }

    // スペクテイターチャットを同じモードのプレイヤーにだけ表示
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        if (sender.getGameMode() != GameMode.SPECTATOR) return;

        event.setCancelled(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) {
                p.sendMessage("[観戦] " + sender.getName() + ": " + event.getMessage());
            }
        }
    }

    // スコア変更の即送信チェック
    private void checkScores() {
        if (!connected.get()) return;

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective autoMuteObj = board.getObjective("Auto.Mute");
        Objective moveCHObj = board.getObjective("Znsi.MoveCH");

        for (Player p : Bukkit.getOnlinePlayers()) {
            int autoMute = autoMuteObj == null ? 0 : autoMuteObj.getScore(p.getName()).getScore();
            int moveCH = moveCHObj == null ? 0 : moveCHObj.getScore(p.getName()).getScore();

            // Auto.Mute の変化を検出
            if (!lastAutoMute.containsKey(p.getName()) || lastAutoMute.get(p.getName()) != autoMute) {
                lastAutoMute.put(p.getName(), autoMute);
                sendScoreChange(p.getName(), autoMute, null);
            }

            // MoveCH の変化を検出
            if (!lastMoveCH.containsKey(p.getName()) || lastMoveCH.get(p.getName()) != moveCH) {
                lastMoveCH.put(p.getName(), moveCH);
                sendScoreChange(p.getName(), null, moveCH);
            }
        }
    }

    // WebSocket 送信
    private void sendScoreChange(String player, Integer autoMute, Integer moveCH) {
        if (!connected.get() || wsClient == null) return;
        JsonObject json = new JsonObject();
        json.addProperty("player", player);
        if (autoMute != null) json.addProperty("autoMute", autoMute);
        if (moveCH != null) json.addProperty("moveCH", moveCH);
        wsClient.send(json.toString());
    }

    // 外部からスコア更新時に呼び出すフック（任意）
    public void onScoreUpdate(Player p, int autoMute, int moveCH) {
        lastAutoMute.put(p.getName(), autoMute);
        lastMoveCH.put(p.getName(), moveCH);
        sendScoreChange(p.getName(), autoMute, moveCH);
    }
}
