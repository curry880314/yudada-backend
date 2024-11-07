package com.yupi.yudada.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 测试 RTMP 流的 WebSocket 连接
 * 作者: liyongkang
 */

@SpringBootTest
public class TestRtmpStream {
    private static final String SERVER_URL = "wss://saasdev.51znyx.com/zyh-service/ask?params=";
    private static final String APP_ID = "testappid";
    private static final String CHANNEL_CODE = "IVR";
    private static final String PHONE_NO = "19802025030";
    private static final String SCENE_CODE = "1";

    /**
     * 测试 WebSocket 连接并获取 RTMP 流地址
     */
    @Test
    void testWebSocketConnection() {
        try {
            WebSocketClient client = new WebSocketClient(new URI(SERVER_URL)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("连接已开启");

                    // 准备并发送初始化请求
                    Map<String, Object> params = createParams();
                    String jsonParams = convertToJson(params);

                    // 发送消息
                    send(jsonParams);
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("收到的响应 body: " + message); // 打印整个响应

                    // 解析响应并提取流地址
                    Map<String, Object> response = parseResponse(message);
                    if (response != null) {
                        String streamAddr = extractStreamAddr(response);
                        Assertions.assertNotNull(streamAddr, "流地址不应为空");
                        Assertions.assertTrue(streamAddr.startsWith("rtmp://"), "流地址格式不正确");

                        // 可选：手动启动播放
                        // playStream(streamAddr);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("连接已关闭，退出代码 " + code + "，原因: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("发生错误: " + ex.getMessage());
                }
            };

            client.connect();
            Thread.sleep(5000); // 等待连接和消息处理
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("WebSocket 连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 创建 WebSocket 请求参数
     */
    private Map<String, Object> createParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", APP_ID);
        params.put("sid", generateSessionId());
        params.put("channelCode", CHANNEL_CODE);
        params.put("phoneNo", PHONE_NO);
        params.put("sceneCode", SCENE_CODE);
        return params;
    }

    /**
     * 将请求参数转换为 JSON 字符串
     */
    private String convertToJson(Map<String, Object> params) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("参数 JSON 转换失败", e);
        }
    }

    /**
     * 生成唯一的会话 ID (使用时间戳 + 随机数)
     */
    private static String generateSessionId() {
        String timeStamp = new java.text.SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date());
        int randomNum = (int) (Math.random() * 10000000);
        return timeStamp + randomNum;
    }

    /**
     * 解析 JSON 响应
     */
    private static Map<String, Object> parseResponse(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从响应中提取流地址
     */
    private String extractStreamAddr(Map<String, Object> response) {
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        if (result != null) {
            Map<String, Object> live = (Map<String, Object>) result.get("live");
            if (live != null && "start:resp".equals(live.get("type"))) {
                String streamAddr = (String) live.get("streamAddr");
                System.out.println("获取到的流地址: " + streamAddr);
                return streamAddr;
            }
        }
        return null;
    }

    /**
     * 使用 ffplay 播放流地址 (手动测试用)
     */
    private static void playStream(String streamAddr) {
        if (streamAddr != null) {
            System.out.println("播放流地址: " + streamAddr);
            try {
                // 使用 ffplay 命令播放流
                String command = "ffplay -i " + streamAddr;
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("未接收到流地址");
        }
    }
}
