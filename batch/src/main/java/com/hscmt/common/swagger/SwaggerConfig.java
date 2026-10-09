package com.hscmt.common.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI batchAPI() {
        OpenAPI openAPI = new OpenAPI();

        Contact contact = new Contact();
        contact.setName("HSCMT");
        contact.setUrl("http://hscmt.co.kr/");

        Info info = new Info()
                .version("v2.0")
                .title("시뮬레이션 Scheduling API")
                .description("온라인 상수관망 의사결정 시뮬레이션 배치 스케줄러 명세")
                .contact(contact);

        openAPI.info(info);
        List<Server> servers = List.of(
                new Server().url("http://localhost:8081").description("로컬서버"),
                new Server().url("http://localhost:8081/batch").description("개발서버"),
                new Server().url("http://localhost:60082/batch").description("운영서버")
        );
        openAPI.servers(servers);

        /* 헤더에 Authorization 으로 jwt 토큰값 묻히기 */
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        /* jwt 토큰 인증 */
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Authorization");

        /* Header에 Authorization 묻히기 */
        openAPI.components(new Components().addSecuritySchemes("Authorization", apiKey))
                .addSecurityItem(securityRequirement);

        return openAPI;
    }
}
