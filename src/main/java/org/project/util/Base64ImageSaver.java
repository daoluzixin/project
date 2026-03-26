package org.project.util;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Base64图片解码保存工具类
 */
@Slf4j
public class Base64ImageSaver {
    
    /**
     * 保存Base64图片到文件
     * 
     * @param base64Image Base64编码的图片字符串
     * @param outputPath 输出文件路径（如：result.jpg）
     * @return 是否保存成功
     */
    public static boolean saveToFile(String base64Image, String outputPath) {
        if (base64Image == null || base64Image.isEmpty()) {
            log.error("Base64图片字符串为空");
            return false;
        }
        
        try {
            // 1. 清理Base64字符串（移除可能的前缀）
            String cleanBase64 = cleanBase64String(base64Image);
            
            // 2. Base64解码
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            
            // 3. 保存到文件
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(imageBytes);
                log.info("图片保存成功: {} ({} bytes)", outputPath, imageBytes.length);
                return true;
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 保存Base64图片到指定目录，自动生成文件名
     * 
     * @param base64Image Base64编码的图片字符串
     * @param outputDir 输出目录
     * @param fileName 文件名（不含扩展名）
     * @return 保存的文件路径，失败返回null
     */
    public static String saveToDirectory(String base64Image, String outputDir, String fileName) {
        try {
            // 创建目录
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 获取图片格式
            String format = getImageFormat(base64Image);
            
            // 构建完整文件路径
            String fullPath = outputDir + File.separator + fileName + "." + format;
            
            // 保存图片
            if (saveToFile(base64Image, fullPath)) {
                return fullPath;
            }
            return null;
            
        } catch (Exception e) {
            log.error("保存图片失败", e);
            return null;
        }
    }
    
    /**
     * 保存Base64图片（带时间戳文件名）
     * 
     * @param base64Image Base64编码的图片字符串
     * @param outputDir 输出目录
     * @param prefix 文件名前缀
     * @return 保存的文件路径
     */
    public static String saveWithTimestamp(String base64Image, String outputDir, String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = prefix + "_" + timestamp;
        return saveToDirectory(base64Image, outputDir, fileName);
    }
    
    /**
     * 清理Base64字符串，移除data:image前缀
     */
    private static String cleanBase64String(String base64Image) {
        String cleaned = base64Image.trim();
        
        // 移除可能的前缀（如 data:image/jpeg;base64,）
        if (cleaned.contains(",")) {
            cleaned = cleaned.split(",")[1];
        }
        
        // 移除可能的引号
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        // 移除换行符和空格
        cleaned = cleaned.replaceAll("\\s+", "");
        cleaned = cleaned.replaceAll("\\n", "");
        cleaned = cleaned.replaceAll("\\r", "");
        
        return cleaned;
    }
    
    /**
     * 获取图片格式
     */
    private static String getImageFormat(String base64Image) {
        String cleanBase64 = cleanBase64String(base64Image);
        
        if (cleanBase64.startsWith("/9j/")) {
            return "jpg";
        } else if (cleanBase64.startsWith("iVBORw0KGgo")) {
            return "png";
        } else if (cleanBase64.startsWith("R0lGODlh")) {
            return "gif";
        } else if (cleanBase64.startsWith("Qk08")) {
            return "bmp";
        } else {
            return "jpg";
        }
    }
    
    /**
     * 获取图片大小（字节）
     */
    public static long getImageSize(String base64Image) {
        try {
            String cleanBase64 = cleanBase64String(base64Image);
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            return imageBytes.length;
        } catch (Exception e) {
            log.error("获取图片大小失败", e);
            return 0;
        }
    }
    
    /**
     * 验证是否为有效的Base64图片
     */
    public static boolean isValidImage(String base64Image) {
        try {
            String cleanBase64 = cleanBase64String(base64Image);
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            return imageBytes != null && imageBytes.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
}