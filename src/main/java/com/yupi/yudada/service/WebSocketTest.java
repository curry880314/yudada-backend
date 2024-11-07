package com.yupi.yudada.service;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WebSocketTest {

    // WebSocket服务器地址
    private static final String SERVER_URL = "wss://saasdev.51znyx.com/zyh-service/ask?params=";
    private static final String APP_ID = "testappid"; // 应用ID
    private static final String CHANNEL_CODE = "IVR"; // 渠道编码
    private static final String PHONE_NO = "19802025030"; // 手机号码
    private static final String SCENE_CODE = "1"; // 场景编码
    private WebSocketClient client;
    private Timer heartBeatTimer;
    private String sessionId; // 用于存储会话ID

    public static void main(String[] args) {
        new WebSocketTest().startWebSocketConnection();
    }

    public void startWebSocketConnection() {
        try {
            // 创建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appId", APP_ID);
            params.put("sid", generateSid()); // 生成初始会话ID
            params.put("channelCode", CHANNEL_CODE);
            params.put("phoneNo", PHONE_NO);
            params.put("sceneCode", SCENE_CODE);
            params.put("startText", "");

            // 使用 Gson 将请求参数转换为 JSON 字符串，并进行 URLEncoder 编码
            Gson gson = new Gson();
            String paramsJson = gson.toJson(params);
            String encodedParams = URLEncoder.encode(paramsJson, StandardCharsets.UTF_8.toString());

            // 完整的请求 URL
            URI serverUri = new URI(SERVER_URL + encodedParams);

            // 创建 WebSocket 客户端
            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("已连接到服务器");
                    startHeartBeat(); // 启动心跳机制
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("收到消息: " + message);

                    try {
                        // 使用 JsonParser 解析响应消息
                        JsonObject jsonResponse = JsonParser.parseString(message).getAsJsonObject();
                        if (jsonResponse.has("result")) {
                            JsonObject result = jsonResponse.getAsJsonObject("result");
                            sessionId = result.get("sid").getAsString(); // 保存会话ID

                            if (result.has("live")) {
                                JsonObject live = result.getAsJsonObject("live");
                                String liveType = live.get("type").getAsString();

                                // 仅在 type 为 "start:resp" 时更新并使用 streamAddr
                                if ("start:resp".equals(liveType) && live.has("streamAddr") && !live.get("streamAddr").isJsonNull()) {
                                    String streamAddr = live.get("streamAddr").getAsString();
                                    System.out.println("推流地址: " + streamAddr);

                                    // 使用 ffplay 播放推流地址
                                    playStreamWithFFplay(streamAddr);
                                } else if ("handle:resp".equals(liveType)) {
                                    // 处理实时问答响应
                                    JsonObject ask = result.getAsJsonObject("ask");
                                    System.out.println("问答回复: " + ask.get("text").getAsString());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("处理 WebSocket 消息时出错: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("连接已关闭。代码: " + code + "，原因: " + reason);
                    stopHeartBeat(); // 停止心跳
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket 错误: ");
                    ex.printStackTrace();
                }
            };

            // 开始连接
            client.connect();

            // 保持 WebSocket 连接，不主动关闭
            while (client.isOpen()) {
                TimeUnit.SECONDS.sleep(1); // 每秒检查一次连接状态
            }

            System.out.println("WebSocket 连接已断开，程序结束。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成唯一的 SID（生成方式：12 位时间戳 + 7 位随机数）
    private static String generateSid() {
        String timestamp = new java.text.SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date());
        int randomNum = (int) (Math.random() * 1_000_0000);
        return timestamp + String.format("%07d", randomNum);
    }

    private void playStreamWithFFplay(String streamAddr) {
        new Thread(() -> {
            try {
                // 构建 ffplay 命令
                String command = "ffplay -i " + streamAddr;

                // 执行命令
                Process process = Runtime.getRuntime().exec(command);

                // 等待 ffplay 播放完成
                process.waitFor();

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("打开 ffplay 播放流时出错: " + e.getMessage());
            }
        }).start();
    }

    // 启动心跳机制
    private void startHeartBeat() {
        heartBeatTimer = new Timer();
        heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendHeartBeat();
            }
        }, 0, 10000); // 每10秒发送一次心跳
    }

    // 停止心跳
    private void stopHeartBeat() {
        if (heartBeatTimer != null) {
            heartBeatTimer.cancel();
            heartBeatTimer = null;
        }
    }

    // 发送心跳消息
    private void sendHeartBeat() {
        if (client != null && client.isOpen() && sessionId != null) {
            Map<String, Object> heartBeatMessage = new HashMap<>();
            heartBeatMessage.put("sid", sessionId); // 使用现有的会话ID
            heartBeatMessage.put("phoneNo", PHONE_NO);
            heartBeatMessage.put("text", "");
            heartBeatMessage.put("type", "heart");
            heartBeatMessage.put("ext", new HashMap<>()); // 空扩展参数

            // 转换为JSON并发送
            String jsonHeartBeat = new Gson().toJson(heartBeatMessage);
            client.send(jsonHeartBeat);
            System.out.println("发送心跳消息: " + jsonHeartBeat);
        }
    }
}
