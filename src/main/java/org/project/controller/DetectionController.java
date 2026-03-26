package org.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.entity.request.DetectionRequest;
import org.project.entity.response.DetectionResponse;
import org.project.entity.response.HealthCheckResponse;
import org.project.service.DetectionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/detection")
@RequiredArgsConstructor
@Tag(name = "双流目标检测", description = "RGB+红外双模态目标检测接口")
public class DetectionController {

    private final DetectionService detectionService;

    @PostMapping(value = "/dualstream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "目标检测", description = "上传RGB和红外图像，返回Base64编码的检测结果")
    public ResponseEntity<DetectionResponse> detect(
            @Parameter(description = "RGB图像", required = true)
            @RequestParam("rgb_file") MultipartFile rgbFile,

            @Parameter(description = "红外图像", required = true)
            @RequestParam("ir_file") MultipartFile irFile,

            @Parameter(description = "置信度阈值 (0.0-1.0)", example = "0.25")
            @RequestParam(value = "conf_thres", required = false, defaultValue = "0.25")
            Float confThres,

            @Parameter(description = "IOU阈值 (0.0-1.0)", example = "0.45")
            @RequestParam(value = "iou_thres", required = false, defaultValue = "0.45")
            Float iouThres) {

        try {
            // 参数校验
            if (rgbFile == null || rgbFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(DetectionResponse.error(400, "RGB图像不能为空"));
            }

            if (irFile == null || irFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(DetectionResponse.error(400, "红外图像不能为空"));
            }

            // 构建请求
            DetectionRequest request = DetectionRequest.builder()
                    .rgbFile(rgbFile)
                    .irFile(irFile)
                    .confThres(confThres)
                    .iouThres(iouThres)
                    .build();

            // 调用检测服务
            DetectionResponse response = detectionService.detect(request);

            if (response.getCode() == 200) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }

        } catch (Exception e) {
            log.error("检测失败", e);
            return ResponseEntity.status(500)
                    .body(DetectionResponse.error(500, "检测失败: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查Python检测服务状态")
    public ResponseEntity<HealthCheckResponse> health() {
        HealthCheckResponse response = detectionService.healthCheck();
        if ("healthy".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }
}