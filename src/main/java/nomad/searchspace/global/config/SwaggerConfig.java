package nomad.searchspace.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class SwaggerConfig {

    @Value("${server.url}")
    private String serverUrl;
    @Bean
    public OpenAPI openAPI() {

        Server server = new Server();
        server.setUrl(serverUrl);

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .servers(List.of(server));
    }
    private Info apiInfo() {
        return new Info()
                .title("공간탐색 API")
                .description("공간탐색 API 입니다.")
                .version("1.0.0");
    }
}

