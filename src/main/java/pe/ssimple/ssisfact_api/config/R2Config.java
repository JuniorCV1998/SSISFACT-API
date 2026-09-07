package pe.ssimple.ssisfact_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class R2Config {

    private final R2Properties r2Properties;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(r2Properties.getEndpoint()))
                // R2 no usa regiones reales, pero el SDK exige una.
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2Properties.getAccessKey(), r2Properties.getSecretKey())))
                .forcePathStyle(true)
                .build();
    }
}
