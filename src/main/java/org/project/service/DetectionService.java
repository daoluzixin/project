package org.project.service;

import org.project.entity.request.DetectionRequest;
import org.project.entity.response.DetectionResponse;
import org.project.entity.response.FolderDetectionResponse;
import org.project.entity.response.HealthCheckResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 双流检测服务接口 - 同步版本
 */
public interface DetectionService {

    /**
     * 同步检测 - 返回Base64编码的结果
     */
    DetectionResponse detect(DetectionRequest request);

    /**
     * 文件夹批量检测
     */
    FolderDetectionResponse detectFolder(List<MultipartFile> rgbFiles,
                                         List<MultipartFile> irFiles, Float confThres, Float iouThres);

    /**
     * 健康检查
     */
    HealthCheckResponse healthCheck();
}