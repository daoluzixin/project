package org.project.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDetectionRequest {

    private List<MultipartFile> rgbFiles;   // RGB图像文件列表
    private List<MultipartFile> irFiles;    // 红外图像文件列表
    private Float confThres;                // 置信度阈值
    private Float iouThres;                 // IOU阈值

    /**
     * 验证请求参数
     */
    public boolean isValid() {
        return rgbFiles != null && !rgbFiles.isEmpty()
                && irFiles != null && !irFiles.isEmpty();
    }
}