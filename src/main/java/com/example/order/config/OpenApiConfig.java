//package com.example.order.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class OpenApiConfig {
//
//    @Value("${spring.application.name}")
//    private String appName;
//
//    @Bean
//    public OpenAPI springOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title(appName)
//                        .description(appName)
//                        .version("1.0.0"));
//    }
//
//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("all")
//                .pathsToMatch("/**")
//                .build();
//    }
//
//}
