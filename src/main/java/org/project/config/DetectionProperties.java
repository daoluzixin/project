package org.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Data
@Component
@ConfigurationProperties(prefix = "detection")
public class DetectionProperties {
    private String url = "http://localhost:8000";
    private Integer connectTimeout = 5000;
    private Integer readTimeout = 60000;
}

