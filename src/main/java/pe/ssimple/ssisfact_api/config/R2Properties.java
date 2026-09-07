package pe.ssimple.ssisfact_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicBaseUrl;
}
