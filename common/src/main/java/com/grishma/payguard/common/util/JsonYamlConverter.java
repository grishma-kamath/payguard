package com.grishma.payguard.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reusable utility for converting between JSON and Swagger-compatible YAML.
 * Handles nested heterogeneous data types including arrays, maps, and primitives.
 * Published internally as a Maven package and adopted by multiple teams.
 */
public final class JsonYamlConverter {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
    ).enable(SerializationFeature.INDENT_OUTPUT);

    private JsonYamlConverter() {}

    public static String jsonToYaml(String json) throws JsonProcessingException {
        JsonNode jsonNode = JSON_MAPPER.readTree(json);
        return YAML_MAPPER.writeValueAsString(jsonNode);
    }

    public static String yamlToJson(String yaml) throws JsonProcessingException {
        JsonNode yamlNode = YAML_MAPPER.readTree(yaml);
        return JSON_MAPPER.writeValueAsString(yamlNode);
    }

    public static Map<String, Object> jsonToMap(String json) throws JsonProcessingException {
        return JSON_MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    public static String mapToYaml(Map<String, Object> map) throws JsonProcessingException {
        return YAML_MAPPER.writeValueAsString(map);
    }

    public static String mapToJson(Map<String, Object> map) throws JsonProcessingException {
        return JSON_MAPPER.writeValueAsString(map);
    }

    public static String generateSwaggerSchema(String json) throws JsonProcessingException {
        JsonNode root = JSON_MAPPER.readTree(json);
        ObjectNode schema = JSON_MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", generateProperties(root));
        return YAML_MAPPER.writeValueAsString(schema);
    }

    private static ObjectNode generateProperties(JsonNode node) {
        ObjectNode properties = JSON_MAPPER.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            ObjectNode prop = JSON_MAPPER.createObjectNode();
            JsonNode value = field.getValue();

            if (value.isTextual()) {
                prop.put("type", "string");
                prop.put("example", value.asText());
            } else if (value.isNumber()) {
                if (value.isInt() || value.isLong()) {
                    prop.put("type", "integer");
                    prop.put("format", value.isLong() ? "int64" : "int32");
                } else {
                    prop.put("type", "number");
                    prop.put("format", "double");
                }
                prop.set("example", value);
            } else if (value.isBoolean()) {
                prop.put("type", "boolean");
                prop.set("example", value);
            } else if (value.isArray()) {
                prop.put("type", "array");
                if (!value.isEmpty()) {
                    ObjectNode items = generateItemSchema(value.get(0));
                    prop.set("items", items);
                }
            } else if (value.isObject()) {
                prop.put("type", "object");
                prop.set("properties", generateProperties(value));
            }

            properties.set(field.getKey(), prop);
        }
        return properties;
    }

    private static ObjectNode generateItemSchema(JsonNode item) {
        ObjectNode schema = JSON_MAPPER.createObjectNode();
        if (item.isTextual()) {
            schema.put("type", "string");
        } else if (item.isNumber()) {
            schema.put("type", "number");
        } else if (item.isBoolean()) {
            schema.put("type", "boolean");
        } else if (item.isObject()) {
            schema.put("type", "object");
            schema.set("properties", generateProperties(item));
        }
        return schema;
    }

    public static JsonNode mergeNodes(JsonNode mainNode, JsonNode updateNode) {
        if (mainNode instanceof ObjectNode mainObject && updateNode instanceof ObjectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = updateNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode existingValue = mainObject.get(field.getKey());
                if (existingValue != null && existingValue.isObject() && field.getValue().isObject()) {
                    mergeNodes(existingValue, field.getValue());
                } else {
                    mainObject.set(field.getKey(), field.getValue());
                }
            }
        }
        return mainNode;
    }
}
