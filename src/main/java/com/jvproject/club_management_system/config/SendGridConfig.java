
package com.jvproject.club_management_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SendGridConfig {

    @Bean
    public RestClient sendGridRestClient(
            @Value("${sendgrid.api-key}") String apiKey) {

        return RestClient.builder()
                .baseUrl("https://api.sendgrid.com")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}
