package org.project.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * Base64图片解码工具类
 * 用于将Base64编码的图片解码并返回给客户端下载
 */
@Slf4j
public class Base64ImageUtil {

    /**
     * 解码Base64图片并返回字节数组
     *
     * @param base64Image Base64编码的图片字符串
     * @return 图片字节数组
     */
    public static byte[] decodeToBytes(String base64Image) {
        try {
            // 移除可能存在的data:image/jpeg;base64,前缀
            String base64Data = cleanBase64String(base64Image);

            // Base64解码
            return Base64.getDecoder().decode(base64Data);

        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败", e);
            throw new RuntimeException("Base64解码失败: " + e.getMessage());
        }
    }

    /**
     * 解码Base64图片并返回输入流
     *
     * @param base64Image Base64编码的图片字符串
     * @return 图片输入流
     */
    public static InputStream decodeToInputStream(String base64Image) {
        byte[] imageBytes = decodeToBytes(base64Image);
        return new ByteArrayInputStream(imageBytes);
    }

    /**
     * 清理Base64字符串，移除可能的前缀
     *
     * @param base64Image 原始Base64字符串
     * @return 清理后的Base64字符串
     */
    public static String cleanBase64String(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalArgumentException("Base64图片字符串不能为空");
        }

        String cleaned = base64Image.trim();

        // 1. 移除 Data URI 前缀
        if (cleaned.contains(",")) {
            cleaned = cleaned.split(",")[1];
        }

        // 2. 移除所有空白字符（包括换行、回车、制表符）
        // 这是保证 Base64 解码成功的核心步骤
        cleaned = cleaned.replaceAll("\\s", "");

        return cleaned;
    }

    /**
     * 获取图片格式
     *
     * @param base64Image Base64编码的图片字符串
     * @return 图片格式（jpeg/png/gif等）
     */
    public static String getImageFormat(String base64Image) {
        String base64Data = cleanBase64String(base64Image);

        // 通过Base64字符串前缀判断图片格式
        if (base64Data.startsWith("/9j/")) {
            return "jpeg";
        } else if (base64Data.startsWith("iVBORw0KGgo")) {
            return "png";
        } else if (base64Data.startsWith("R0lGODlh")) {
            return "gif";
        } else if (base64Data.startsWith("Qk08")) {
            return "bmp";
        } else {
            return "jpeg"; // 默认返回jpeg
        }
    }

    /**
     * 获取ContentType
     *
     * @param format 图片格式
     * @return ContentType
     */
    public static MediaType getContentType(String format) {
        switch (format.toLowerCase()) {
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "bmp":
                return MediaType.parseMediaType("image/bmp");
            default:
                return MediaType.IMAGE_JPEG;
        }
    }

    /**
     * 构建图片下载响应
     *
     * @param base64Image Base64编码的图片
     * @param fileName 下载的文件名（不含扩展名）
     * @return ResponseEntity
     */
    public static ResponseEntity<byte[]> buildImageResponse(String base64Image, String fileName) {
        try {
            // 解码图片
            byte[] imageBytes = decodeToBytes(base64Image);

            // 获取图片格式
            String format = getImageFormat(base64Image);
            MediaType contentType = getContentType(format);

            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentLength(imageBytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "." + format + "\"");

            // 返回响应
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageBytes);

        } catch (Exception e) {
            log.error("构建图片响应失败", e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 构建图片预览响应（内联显示）
     *
     * @param base64Image Base64编码的图片
     * @return ResponseEntity
     */
    public static ResponseEntity<byte[]> buildInlineImageResponse(String base64Image) {
        try {
            byte[] imageBytes = decodeToBytes(base64Image);
            String format = getImageFormat(base64Image);
            MediaType contentType = getContentType(format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentLength(imageBytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageBytes);

        } catch (Exception e) {
            log.error("构建图片预览响应失败", e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }


}