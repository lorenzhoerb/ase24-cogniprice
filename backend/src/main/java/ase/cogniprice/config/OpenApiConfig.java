package ase.cogniprice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.server.url}")
    private String serverUrl;

    @Value("${openapi.server.description:Default Server Description}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
           .info(new Info()
               .title("CogniPrice API")
               .description("""
                Access CogniPrice API using JWT or API Key in the Authorization header.
                
                API Key Example: `apiKey <your_api_key>`
                """)
               .version("1.0.0"))
           .addSecurityItem(new SecurityRequirement().addList("apiKey"))
           .components(new Components()
               .addSecuritySchemes("apiKey",
                   new SecurityScheme()
                       .type(SecurityScheme.Type.APIKEY)
                       .in(SecurityScheme.In.HEADER)
                       .name("Authorization")))
            .addServersItem(new Server()
                .url(serverUrl)
                .description(serverDescription)); // Dynamically added server
    }
}