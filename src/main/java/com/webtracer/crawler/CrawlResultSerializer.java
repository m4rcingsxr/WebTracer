package com.webtracer.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webtracer.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class to serialize a {@link CrawlResult} to JSON and write it to a file or output stream.
 *
 * <p>This class handles the serialization of a {@link CrawlResult} object into JSON format and
 * provides methods to write the serialized data either to a file or to an arbitrary {@link Writer}.
 * It ensures that the output is correctly formatted and ready for further processing or storage.
 * </p>
 *
 * @param <T> The type of {@link CrawlResult} that will be serialized. This allows the class to be used
 *            with any subclass of {@link CrawlResult}.
 */
@RequiredArgsConstructor
@Slf4j
public final class CrawlResultSerializer<T extends CrawlResult> {

    /**
     * The {@link CrawlResult} object to be serialized.
     */
    @NonNull
    private final T result;

    /**
     * Serializes the {@link CrawlResult} to JSON and writes it to the specified {@link Path}.
     *
     * <p>This method creates or opens the file at the given path and writes the serialized
     * JSON content of the {@link CrawlResult} into it. If the file already exists, it will
     * be overwritten.</p>
     *
     * @param outputPath the path to the file where the serialized crawl result should be written.
     * @throws ApiException if an I/O error occurs during writing to the file.
     * @throws NullPointerException if {@code outputPath} is {@code null}.
     */
    public void saveToPath(Path outputPath) throws ApiException {
        Objects.requireNonNull(outputPath, "Output path cannot be null");
        log.debug("Saving crawl result to path: {}", outputPath);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            saveToWriter(writer);
        } catch (IOException e) {
            log.error("Failed to save crawl result to path: {}", outputPath, e);
            throw new ApiException("Failed to save crawl result to path: " + outputPath, e);
        }
    }

    /**
     * Serializes the {@link CrawlResult} to JSON and writes it to the provided {@link Writer}.
     *
     * <p>This method serializes the {@link CrawlResult} and writes the resulting JSON
     * to the given writer, which could be a file writer, a network socket writer, or any
     * other type of {@link Writer}.</p>
     *
     * @param outputWriter the writer where the serialized crawl result should be written.
     * @throws ApiException if an I/O error occurs during writing to the writer.
     * @throws NullPointerException if {@code outputWriter} is {@code null}.
     */
    public void saveToWriter(Writer outputWriter) throws ApiException {
        Objects.requireNonNull(outputWriter, "Output writer cannot be null");
        log.debug("Serializing crawl result to writer");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(outputWriter, result);
            log.info("Crawl result successfully serialized");
        } catch (IOException e) {
            log.error("Failed to serialize crawl result to writer", e);
            throw new ApiException("Failed to serialize crawl result to writer", e);
        }
    }

}
