package com.smartlogis.orderservice.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {


	private final String PREFIX = "/v1/orders";

	@Bean
	public OpenAPI openAPI(@Value("${openapi.service.url}") String url) {
		return new OpenAPI()
			.servers(List.of(new Server().url(url)))
			.components(new Components().addSecuritySchemes("Bearer", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
			.addSecurityItem(new SecurityRequirement().addList("Bearer"))
			.info(new Info().title("주문 서비스")
				.description("Order API"));
	}

	@Bean
	public OpenApiCustomizer addPrefixToPaths() {
		return openApi -> {
			Paths paths = openApi.getPaths();
			if (paths == null) return;

			Map<String, PathItem> original = new LinkedHashMap<>(paths);
			for (String path : original.keySet().toArray(new String[0])) {
				String prefixed = PREFIX + path;
				if (!paths.containsKey(prefixed)) {
					PathItem item = original.get(path);
					paths.addPathItem(prefixed, item);
				}
			}

			original.keySet().forEach(s -> {
				if (!s.startsWith(PREFIX)) paths.remove(s);
			});
		};
	}
}
