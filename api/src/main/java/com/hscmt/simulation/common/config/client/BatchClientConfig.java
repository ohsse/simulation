package com.hscmt.simulation.common.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class BatchClientConfig {

    @Value("${batch.url}")
    private String batchUrl;

    @Bean(name = "batchWebClient")
    public WebClient batchWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(batchUrl+"/internal")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .wiretap(true)
                                .compress(true)
                                .responseTimeout(Duration.ofSeconds(10))
                ))
                .defaultHeaders(h -> {
                    h.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    h.add("X-Internal-Request", "true");
                })
                .build();

    }
}
