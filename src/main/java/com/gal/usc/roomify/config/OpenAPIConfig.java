package com.gal.usc.roomify.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Roomify API")
                        .version("1.0.0")
                        .description("""
                                Documentación de Roomify con Scalar
                                
                                ### Desarrolladores:
                                - **Xenxo Fernández Rodríguez** - xenxo.fernandez@rai.usc.es
                                - **Iván García González** - ivan.garcia.gonzalez3@rai.usc.es
                                """)
                        .contact(new Contact()
                                .name("Xenxo & Iván")
                                ));
    }
}