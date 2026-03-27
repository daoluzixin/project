package org.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.config.DetectionProperties;
import org.project.entity.request.DetectionRequest;
import org.project.entity.response.DetectionResponse;
import org.project.entity.response.FolderDetectionResponse;
import org.project.entity.response.HealthCheckResponse;
import org.project.service.DetectionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 文件夹批量检测
     */
    @Override
    public FolderDetectionResponse detectFolder(List<MultipartFile> rgbFiles,
                                                List<MultipartFile> irFiles, Float confThres, Float iouThres) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证请求参数
            if (rgbFiles == null || rgbFiles.isEmpty() || irFiles == null || irFiles.isEmpty()) {
                log.warn("请求参数无效: RGB或红外文件列表为空");
                return FolderDetectionResponse.error(400, "RGB和红外文件列表都不能为空");
            }

            // 2. 按文件名匹配RGB和红外图像
            Map<String, MultipartFile> rgbMap = rgbFiles.stream()
                    .collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));
            Map<String, MultipartFile> irMap = irFiles.stream()
                    .collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));

            // 3. 遍历处理每对图像
            List<FolderDetectionResponse.SingleResult> results = new ArrayList<>();
            int successCount = 0;
            int failedCount = 0;

            for (String fileName : rgbMap.keySet()) {
                if (irMap.containsKey(fileName)) {
                    try {
                        // 构建单张图片的检测请求
                        DetectionRequest singleRequest = DetectionRequest.builder()
                                .rgbFile(rgbMap.get(fileName))
                                .irFile(irMap.get(fileName))
                                .confThres(confThres)
                                .iouThres(iouThres)
                                .build();

                        // 调用单张检测方法
                        DetectionResponse response = detect(singleRequest);

                        if (response.getCode() == 200) {
                            results.add(FolderDetectionResponse.SingleResult.builder()
                                    .fileName(fileName)
                                    .resultImage(response.getResultImage())
                                    .inferenceTime(response.getInferenceTime())
                                    .success(true)
                                    .build());
                            successCount++;
                        } else {
                            results.add(FolderDetectionResponse.SingleResult.builder()
                                    .fileName(fileName)
                                    .success(false)
                                    .errorMessage(response.getMessage())
                                    .build());
                            failedCount++;
                        }
                    } catch (Exception e) {
                        log.error("处理文件失败: {}", fileName, e);
                        results.add(FolderDetectionResponse.SingleResult.builder()
                                .fileName(fileName)
                                .success(false)
                                .errorMessage(e.getMessage())
                                .build());
                        failedCount++;
                    }
                } else {
                    log.warn("未找到匹配的红外图像: {}", fileName);
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("文件夹检测完成 - 总数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                    results.size(), successCount, failedCount, totalTime);

            return FolderDetectionResponse.success(results.size(), successCount, failedCount, totalTime, results);

        } catch (Exception e) {
            log.error("文件夹检测失败", e);
            return FolderDetectionResponse.error(500, "文件夹检测失败: " + e.getMessage());
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