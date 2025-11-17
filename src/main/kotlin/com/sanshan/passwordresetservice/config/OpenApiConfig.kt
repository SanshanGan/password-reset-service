package com.sanshan.passwordresetservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Password Reset Service API")
                    .version("1.0")
                    .description("Secure RESTful API for handling password reset requests with time-limited, single-use tokens")
            )
    }
}
