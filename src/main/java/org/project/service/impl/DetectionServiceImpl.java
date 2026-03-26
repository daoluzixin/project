package org.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.config.DetectionProperties;
import org.project.entity.request.DetectionRequest;
import org.project.entity.response.DetectionResponse;
import org.project.entity.response.HealthCheckResponse;
import org.project.service.DetectionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionServiceImpl implements DetectionService {

    private final RestTemplate restTemplate;
    private final DetectionProperties properties;

    /**
     * 同步检测方法 - 返回Base64编码的图片
     */
    @Override
    public DetectionResponse detect(DetectionRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证请求参数
            if (!request.isValid()) {
                log.warn("请求参数无效: RGB或红外图像为空");
                return DetectionResponse.error(400, "RGB和红外图像都不能为空");
            }

            log.info("开始检测 - RGB文件: {}, IR文件: {}, conf: {}, iou: {}",
                    request.getRgbFile().getOriginalFilename(),
                    request.getIrFile().getOriginalFilename(),
                    request.getConfThres(),
                    request.getIouThres());

            // 2. 构建HTTP请求
            HttpEntity<MultiValueMap<String, Object>> httpEntity = buildHttpEntity(request);

            // 3. 调用Python检测服务
            String url = properties.getUrl() + "/predict/image";

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    byte[].class
            );

            // 4. 处理响应
            long inferenceTime = System.currentTimeMillis() - startTime;

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("检测成功 - 耗时: {}ms, 结果大小: {} bytes",
                        inferenceTime, response.getBody().length);

                // 返回Base64编码的图片
                return DetectionResponse.success(response.getBody(), inferenceTime);

            } else {
                log.error("检测服务返回错误，状态码: {}", response.getStatusCode());
                return DetectionResponse.error(500, "检测服务返回错误");
            }

        } catch (Exception e) {
            log.error("检测失败", e);

            // 根据异常类型返回不同的错误码
            if (e instanceof java.net.ConnectException) {
                return DetectionResponse.error(503, "无法连接检测服务，请确保Python服务已启动");
            } else if (e instanceof java.net.SocketTimeoutException) {
                return DetectionResponse.error(504, "检测超时，请稍后重试");
            }

            return DetectionResponse.error(500, "检测失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @Override
    public HealthCheckResponse healthCheck() {
        try {
            String url = properties.getUrl() + "/health";
            log.debug("执行健康检查: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("检测服务健康");
                return HealthCheckResponse.healthy(true, "1.0.0");
            } else {
                log.warn("健康检查返回非200状态码: {}", response.getStatusCode());
                return HealthCheckResponse.unhealthy();
            }

        } catch (Exception e) {
            log.error("健康检查失败: {}", e.getMessage());
            return HealthCheckResponse.unhealthy();
        }
    }

    /**
     * 构建HTTP请求实体
     */
    private HttpEntity<MultiValueMap<String, Object>> buildHttpEntity(DetectionRequest request)
            throws IOException {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 1. 添加RGB图像
        byte[] rgbBytes = request.getRgbFile().getBytes();
        String rgbFilename = request.getRgbFile().getOriginalFilename();
        body.add("rgb_file", new ByteArrayResource(rgbBytes) {
            @Override
            public String getFilename() {
                return rgbFilename;
            }
        });

        // 2. 添加红外图像
        byte[] irBytes = request.getIrFile().getBytes();
        String irFilename = request.getIrFile().getOriginalFilename();
        body.add("ir_file", new ByteArrayResource(irBytes) {
            @Override
            public String getFilename() {
                return irFilename;
            }
        });

        // 3. 添加置信度阈值
        Float confThres = request.getConfThres();
        if (confThres != null && confThres > 0 && confThres < 1) {
            body.add("conf_thres", confThres.toString());
        } else {
            body.add("conf_thres", "0.25");
        }

        // 4. 添加IOU阈值
        Float iouThres = request.getIouThres();
        if (iouThres != null && iouThres > 0 && iouThres < 1) {
            body.add("iou_thres", iouThres.toString());
        } else {
            body.add("iou_thres", "0.45");
        }

        // 5. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        log.debug("构建请求完成 - RGB文件: {}, IR文件: {}, conf: {}, iou: {}",
                rgbFilename, irFilename,
                body.get("conf_thres").get(0),
                body.get("iou_thres").get(0));

        return new HttpEntity<>(body, headers);
    }
}