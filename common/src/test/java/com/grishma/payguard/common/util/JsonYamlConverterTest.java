package com.grishma.payguard.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonYamlConverterTest {

    @Test
    void shouldConvertJsonToYaml() throws JsonProcessingException {
        String json = """
                {"name": "PayGuard", "version": "3.0", "active": true}
                """;
        String yaml = JsonYamlConverter.jsonToYaml(json);
        assertNotNull(yaml);
        assertTrue(yaml.contains("name:"));
        assertTrue(yaml.contains("PayGuard"));
        assertTrue(yaml.contains("version:"));
    }

    @Test
    void shouldConvertYamlToJson() throws JsonProcessingException {
        String yaml = """
                name: PayGuard
                version: "3.0"
                active: true
                """;
        String json = JsonYamlConverter.yamlToJson(yaml);
        assertNotNull(json);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("PayGuard"));
    }

    @Test
    void shouldConvertJsonToMap() throws JsonProcessingException {
        String json = """
                {"service": "transaction-service", "port": 8081}
                """;
        Map<String, Object> map = JsonYamlConverter.jsonToMap(json);
        assertEquals("transaction-service", map.get("service"));
        assertEquals(8081, map.get("port"));
    }

    @Test
    void shouldConvertMapToYaml() throws JsonProcessingException {
        Map<String, Object> map = Map.of("host", "localhost", "port", 5432);
        String yaml = JsonYamlConverter.mapToYaml(map);
        assertNotNull(yaml);
        assertTrue(yaml.contains("host:"));
        assertTrue(yaml.contains("localhost"));
    }

    @Test
    void shouldConvertMapToJson() throws JsonProcessingException {
        Map<String, Object> map = Map.of("service", "fraud-detection", "enabled", true);
        String json = JsonYamlConverter.mapToJson(map);
        assertNotNull(json);
        assertTrue(json.contains("\"service\""));
        assertTrue(json.contains("fraud-detection"));
    }

    @Test
    void shouldHandleNestedJson() throws JsonProcessingException {
        String json = """
                {
                    "database": {
                        "host": "localhost",
                        "port": 5432,
                        "credentials": {
                            "username": "payguard",
                            "password": "secret"
                        }
                    },
                    "kafka": {
                        "bootstrap": "localhost:9092",
                        "topics": ["transactions", "fraud-results"]
                    }
                }
                """;
        String yaml = JsonYamlConverter.jsonToYaml(json);
        assertNotNull(yaml);
        assertTrue(yaml.contains("database:"));
        assertTrue(yaml.contains("credentials:"));
        assertTrue(yaml.contains("kafka:"));

        String backToJson = JsonYamlConverter.yamlToJson(yaml);
        assertTrue(backToJson.contains("\"database\""));
    }

    @Test
    void shouldGenerateSwaggerSchema() throws JsonProcessingException {
        String json = """
                {
                    "id": "TXN001",
                    "amount": 5000.0,
                    "type": "DEBIT",
                    "blocked": false,
                    "items": [{"name": "item1"}]
                }
                """;
        String schema = JsonYamlConverter.generateSwaggerSchema(json);
        assertNotNull(schema);
        assertTrue(schema.contains("type: object"));
        assertTrue(schema.contains("properties:"));
        assertTrue(schema.contains("string"));
        assertTrue(schema.contains("number"));
        assertTrue(schema.contains("boolean"));
    }

    @Test
    void shouldHandleEmptyJson() throws JsonProcessingException {
        String json = "{}";
        String yaml = JsonYamlConverter.jsonToYaml(json);
        assertNotNull(yaml);
    }

    @Test
    void shouldThrowOnInvalidJson() {
        assertThrows(JsonProcessingException.class, () ->
                JsonYamlConverter.jsonToYaml("not valid json")
        );
    }

    @Test
    void shouldRoundTripConversion() throws JsonProcessingException {
        String originalJson = """
                {"name": "test", "value": 42, "nested": {"key": "val"}}
                """;
        String yaml = JsonYamlConverter.jsonToYaml(originalJson);
        String roundTrippedJson = JsonYamlConverter.yamlToJson(yaml);

        Map<String, Object> original = JsonYamlConverter.jsonToMap(originalJson);
        Map<String, Object> roundTripped = JsonYamlConverter.jsonToMap(roundTrippedJson);
        assertEquals(original.get("name"), roundTripped.get("name"));
        assertEquals(original.get("value"), roundTripped.get("value"));
    }
}
