package com.nel.onepiece.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderRenderer {

    private final Map<String, String> placeholders;

    public PlaceholderRenderer(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    public Map<String, Object> renderMap(Map<String, Object> input) {
        Object rendered = renderAny(input);
        if (!(rendered instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Rendered value is not a map");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!(e.getKey() instanceof String k)) {
                continue;
            }
            out.put(k, (Object) e.getValue());
        }
        return out;
    }

    private Object renderAny(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof String s) {
            return renderString(s);
        }
        if (v instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!(e.getKey() instanceof String k)) {
                    continue;
                }
                out.put(k, renderAny(e.getValue()));
            }
            return out;
        }
        if (v instanceof List<?> list) {
            List<Object> out = new ArrayList<>();
            for (Object item : list) {
                out.add(renderAny(item));
            }
            return out;
        }
        return v;
    }

    private String renderString(String input) {
        String out = input;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value == null) {
                continue;
            }
            out = out.replace("${" + key + "}", value);
        }
        return out;
    }
}

