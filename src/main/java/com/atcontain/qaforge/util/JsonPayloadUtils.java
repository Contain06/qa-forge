package com.atcontain.qaforge.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.util.StringUtils;

public final class JsonPayloadUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonPayloadUtils() {
    }

    public static String toJsonString(JsonNode node) {
        return toJsonString(node, "{}");
    }

    public static String toJsonString(JsonNode node, String defaultJson) {
        if (node == null || node.isNull()) {
            return defaultJson;
        }
        if (node.isTextual()) {
            String text = node.asText();
            return StringUtils.hasText(text) ? text : defaultJson;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON payload cannot be converted to string");
        }
    }

    public static JsonNode toJsonNode(String json) {
        if (!StringUtils.hasText(json)) {
            return JsonNodeFactory.instance.objectNode();
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            return TextNode.valueOf(json);
        }
    }
}
