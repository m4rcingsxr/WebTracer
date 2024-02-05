package com.webtracer.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

/**
 * A custom deserializer for the {@link java.time.Duration} class.
 * <p>
 * This deserializer converts an integer value from JSON into a {@link java.time.Duration} object,
 * interpreting the integer as the number of seconds.
 * </p>
 *
 * <pre>
 * Example JSON input:
 * {
 *     "timeoutSeconds": 3600
 * }
 * </pre>
 */
public class DurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        int seconds = p.getIntValue();
        return Duration.ofSeconds(seconds);
    }
}