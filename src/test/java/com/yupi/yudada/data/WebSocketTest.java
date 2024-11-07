package com.yupi.yudada.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
@SpringBootTest
public class WebSocketTest {

    // WebSocket服务器地址
    private static final String SERVER_URL = "wss://saasdev.51znyx.com/zyh-service/ask?params=";
    private static final String APP_ID = "testappid"; // 应用ID
    private static final String CHANNEL_CODE = "IVR"; // 渠道编码
    private static final String PHONE_NO = "19802025030"; // 手机号码
    private static final String SCENE_CODE = "1"; // 场景编码

    public static void main(String[] args) {
        try {
            // 创建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appId", APP_ID);
            params.put("sid", generateSid());
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
            WebSocketClient client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("已连接到服务器");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("收到消息: " + message);

                    // 使用 JsonParser 解析响应消息
                    JsonObject jsonResponse = JsonParser.parseString(message).getAsJsonObject();
                    if (jsonResponse.has("result")) {
                        JsonObject result = jsonResponse.getAsJsonObject("result");
                        if (result.has("live")) {
                            JsonObject live = result.getAsJsonObject("live");
                            if (live.has("streamAddr")) {
                                // 提取 streamAddr 并打印
                                String streamAddr = live.get("streamAddr").getAsString();
                                System.out.println("推流地址: " + streamAddr);
                            }
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("连接已关闭。代码: " + code + "，原因: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            // 开始连接
            client.connect();

            // 等待连接建立后发送请求或接收响应

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
}
