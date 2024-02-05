package com.webtracer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

/**
 * A utility class responsible for loading and parsing the web crawler configuration from a JSON file.
 * <p>
 * This class provides functionality to load a {@link WebCrawlerConfig} from a specified file path,
 * ensuring that the configuration is correctly parsed and made available to the application.
 * </p>
 */
@RequiredArgsConstructor
public final class ConfigFileLoader {

    /**
     * The path to the JSON configuration file.
     * <p>
     * This path is provided during the instantiation of the class and is used to locate the
     * configuration file that will be parsed into a {@link WebCrawlerConfig} object.
     * </p>
     */
    @NonNull
    private final Path path;

    /**
     * Loads the web crawler configuration from the specified file path.
     * <p>
     * This method attempts to open the configuration file located at the provided path,
     * parse its contents, and return a {@link WebCrawlerConfig} instance populated with
     * the configuration values defined in the JSON file.
     * </p>
     *
     * @return the loaded {@link WebCrawlerConfig} object containing the crawler configuration.
     * @throws IOException if there is an issue reading the file or parsing the JSON content.
     */
    public WebCrawlerConfig fetchConfig() throws IOException {
        try (FileReader fileReader = new FileReader(path.toFile())) {
            return read(fileReader);
        }
    }

    /**
     * Parses the web crawler configuration from a given {@link Reader}.
     * <p>
     * This static method takes a {@link Reader} that provides the JSON content and uses it to
     * create a new {@link WebCrawlerConfig} object. It is useful for testing or when the
     * configuration content is already available in memory.
     * </p>
     *
     * @param configReader a {@link Reader} pointing to a JSON string that contains the crawler configuration.
     * @return a {@link WebCrawlerConfig} object populated with values parsed from the JSON content.
     * @throws IOException if there is an issue parsing the JSON content.
     */
    public static WebCrawlerConfig read(Reader configReader) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(configReader, WebCrawlerConfig.class);
    }
}