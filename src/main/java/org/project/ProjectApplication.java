package org.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("✅ Java后端服务启动成功！");
        System.out.println("📡 检测接口: http://localhost:8101/api/detection/dualstream");
        System.out.println("💚 健康检查: http://localhost:8101/api/detection/health");
        System.out.println("========================================\n");
    }
}