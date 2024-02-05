package com.webtracer.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFileLoaderTest {

    @Test
    void givenValidConfigFile_whenFetchConfig_thenConfigIsLoadedCorrectly() throws IOException {
        Path configFilePath = Path.of("src/test/resources/example_config.json");


        ConfigFileLoader configFileLoader = new ConfigFileLoader(configFilePath);
        WebCrawlerConfig config = configFileLoader.fetchConfig();

        assertNotNull(config);
        assertEquals(1, config.getInitialPages().size());
        assertEquals("http://example.com", config.getInitialPages().get(0));
        assertEquals(5, config.getMaxDepth());
        assertEquals(120, config.getTimeout().getSeconds());
        assertEquals(10, config.getPopularWordCount());
        assertEquals("/path/to/results.json", config.getCrawlResultPath());
    }

    @Test
    void givenInvalidConfigFile_whenFetchConfig_thenIOExceptionIsThrown() throws IOException {
        Path configFilePath = Path.of("src/test/resources/invalid_config.json");

        ConfigFileLoader configFileLoader = new ConfigFileLoader(configFilePath);

        assertThrows(IOException.class, configFileLoader::fetchConfig);
    }

    @Test
    void givenNonExistentFile_whenFetchConfig_thenIOExceptionIsThrown() {
        Path nonExistentFilePath = Path.of("src/test/resources/invalid_config.json");

        ConfigFileLoader configFileLoader = new ConfigFileLoader(nonExistentFilePath);

        assertThrows(IOException.class, configFileLoader::fetchConfig);
    }

}