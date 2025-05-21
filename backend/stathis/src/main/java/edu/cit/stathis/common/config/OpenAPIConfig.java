package edu.cit.stathis.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Stathis API")
                .version("1.0")
                .description("Documentation for Stathis backend")
                .contact(
                    new Contact()
                        .name("Stathis Dev Team")
                        .email("support@stathis.edu.cit")
                        .url("https://stathis.edu.cit"))
                .license(new License().name("Apache 2.0").url("http://springdoc.org")));
  }
}
