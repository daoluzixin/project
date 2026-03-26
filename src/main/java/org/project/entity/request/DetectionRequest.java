package org.project.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionRequest {

    private MultipartFile rgbFile;      // RGB图像文件
    private MultipartFile irFile;       // 红外图像文件
    private Float confThres;            // 置信度阈值
    private Float iouThres;             // IOU阈值

    /**
     * 验证请求参数
     */
    public boolean isValid() {
        return rgbFile != null && !rgbFile.isEmpty()
                && irFile != null && !irFile.isEmpty();
    }
}