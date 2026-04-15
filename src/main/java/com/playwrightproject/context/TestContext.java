package com.playwrightproject.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playwrightproject.base.TestBase;
import com.playwrightproject.utils.RuntimeMemoryManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestContext {

    private final TestBase testBase;
    private Map<String, String> scenarioData = new HashMap<>();
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^\\$\\{(.+)}$");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public TestContext() {
        testBase = new TestBase();
    }

    public TestBase getTestBase() {
        return testBase;
    }

    public void setScenarioData(Map<String, String> data) {
        if (data == null) {
            this.scenarioData = new HashMap<>();
            return;
        }
        this.scenarioData = new HashMap<>(data);
    }

    public Map<String, String> getScenarioData() {
        return Collections.unmodifiableMap(scenarioData);
    }

    public void setRuntimeValue(String key, String value) {
        RuntimeMemoryManager.setPropertyInMemory(key, value);
    }

    public String getRuntimeValue(String key) {
        return RuntimeMemoryManager.getPropertyFromMemory(key);
    }

    /**
     * Resolves a simple ${key} placeholder to its string value.
     * If the value is not a placeholder, it is returned as-is.
     */
    public String resolveValue(String value) {
        if (value == null) {
            return null;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            return value;
        }

        String key = matcher.group(1);
        String resolved = scenarioData.get(key);
        if (resolved == null) {
            resolved = RuntimeMemoryManager.getPropertyFromMemory(key);
        }
        if (resolved == null) {
            throw new IllegalArgumentException(
                "No value found for key: " + key + " in scenario data or runtime memory.");
        }
        return resolved;
    }

    /**
     * Resolves a ${key} placeholder whose value is a nested JSON object,
     * and returns it as a Map<String, String>.
     * Use this for step definitions that need address/table-like nested data.
     */
    public Map<String, String> resolveMap(String value) {
        String json = resolveValue(value);
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Value for placeholder '" + value + "' is not a valid JSON object. Raw value: " + json, e);
        }
    }
}
