package org.project.entity.enums;

import lombok.Getter;

/**
 * 检测状态枚举
 * 
 * @author your-name
 * @date 2026-03-21
 */
@Getter
public enum DetectionStatus {
    
    SUCCESS(200, "检测成功"),
    INVALID_PARAM(400, "参数错误"),
    SERVICE_UNAVAILABLE(503, "检测服务不可用"),
    TIMEOUT(504, "检测超时"),
    INTERNAL_ERROR(500, "服务器内部错误");
    
    private final Integer code;
    private final String message;
    
    DetectionStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 根据code获取枚举
     */
    public static DetectionStatus fromCode(Integer code) {
        for (DetectionStatus status : DetectionStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return INTERNAL_ERROR;
    }
}