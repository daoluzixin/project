package org.project.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResponse {

    private Integer code;           // 状态码
    private String message;         // 消息
    private String resultImage;     // Base64编码的结果图片
    private Long inferenceTime;     // 推理耗时（毫秒）
    private Long timestamp;         // 时间戳

    /**
     * 创建成功响应（返回Base64图片）
     */
    public static DetectionResponse success(byte[] imageBytes, Long inferenceTime) {
        return DetectionResponse.builder()
                .code(200)
                .message("success")
                .resultImage(Base64.getEncoder().encodeToString(imageBytes))
                .inferenceTime(inferenceTime)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误响应
     */
    public static DetectionResponse error(Integer code, String message) {
        return DetectionResponse.builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}