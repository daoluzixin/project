package org.project.service;

import org.project.entity.request.DetectionRequest;
import org.project.entity.response.DetectionResponse;
import org.project.entity.response.HealthCheckResponse;

/**
 * 双流检测服务接口 - 同步版本
 */
public interface DetectionService {

    /**
     * 同步检测 - 返回Base64编码的结果
     */
    DetectionResponse detect(DetectionRequest request);

    /**
     * 健康检查
     */
    HealthCheckResponse healthCheck();
}