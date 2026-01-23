package com.booklovers.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI bookLoversOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Lovers API")
                        .description("REST API dla platformy społecznościowej dla czytelników - Book Lovers Community. " +
                                "Większość endpointów wymaga autoryzacji - musisz być zalogowany. " +
                                "Najpierw zaloguj się używając endpointu /api/auth/login, a następnie użyj przycisku 'Authorize' w Swaggerze.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Book Lovers Team")
                                .email("support@booklovers.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.booklovers.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Sesja użytkownika uzyskana po zalogowaniu przez endpoint /api/auth/login. " +
                                        "Po zalogowaniu, sesja jest przechowywana w cookie JSESSIONID. " +
                                        "W Swaggerze użyj przycisku 'Authorize' i wprowadź wartość cookie JSESSIONID.")));
    }
}
