package org.project.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDetectionResponse {

    private Integer code;                    // 状态码
    private String message;                  // 消息
    private Integer totalCount;              // 总图片数
    private Integer successCount;            // 成功处理数
    private Integer failedCount;             // 失败数
    private Long totalTime;                  // 总耗时（毫秒）
    private List<SingleResult> results;      // 单张图片检测结果列表
    private Long timestamp;                  // 时间戳

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleResult {
        private String fileName;             // 文件名
        private String resultImage;          // Base64编码的结果图片
        private Long inferenceTime;          // 单张推理耗时
        private Boolean success;             // 是否成功
        private String errorMessage;         // 错误信息
    }

    /**
     * 创建成功响应
     */
    public static FolderDetectionResponse success(Integer total, Integer success,
            Integer failed, Long totalTime, List<SingleResult> results) {
        return FolderDetectionResponse.builder()
                .code(200)
                .message("success")
                .totalCount(total)
                .successCount(success)
                .failedCount(failed)
                .totalTime(totalTime)
                .results(results)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误响应
     */
    public static FolderDetectionResponse error(Integer code, String message) {
        return FolderDetectionResponse.builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}