package dev.stocky37.xiv.actions.config;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static dev.stocky37.xiv.actions.config.JsonConfiguration.configureFeatures;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import dev.stocky37.xiv.actions.model.Ability;
import io.quarkus.logging.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Produces;

public class Data {

	private final static ObjectMapper yaml = initMapper();


	private static ObjectMapper initMapper() {
		return YAMLMapper.builder()
			.disable(FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
			.addModule(new JavaTimeModule())
			.build();
	}


	@Produces
	@Named("data.actions")
	@Singleton
	@SuppressWarnings("UnstableApiUsage")
	public Map<String, Ability> loadActionData() {
		try {
			return yaml.readValue(
				Resources.getResource("abilities.yml"),
				new TypeReference<>() {}
			);
		} catch (IllegalArgumentException | IOException e) {
			Log.info("Failed to load rotation data", e);
			return new HashMap<>();
		}
	}
}
