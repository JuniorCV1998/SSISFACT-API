package pe.ssimple.ssisfact_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${sunat.bot.timeout-ms}") long sunatBotTimeoutMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(sunatBotTimeoutMs))
                .setReadTimeout(Duration.ofMillis(sunatBotTimeoutMs))
                .build();
    }
}
