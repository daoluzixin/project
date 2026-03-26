package org.project.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {
    private String status;      // healthy/unhealthy
    private Boolean modelLoaded; // 模型是否加载
    private String version;     // 版本号
    private Long timestamp;     // 时间戳

    public static HealthCheckResponse healthy(Boolean modelLoaded, String version) {
        return HealthCheckResponse.builder()
                .status("healthy")
                .modelLoaded(modelLoaded)
                .version(version)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static HealthCheckResponse unhealthy() {
        return HealthCheckResponse.builder()
                .status("unhealthy")
                .modelLoaded(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}