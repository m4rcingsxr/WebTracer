package com.webtracer.main.crawler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class to serialize a {@link CrawlResult} to JSON and write it to a file or output stream.
 *
 * <p>This class handles the serialization of a {@link CrawlResult} object into a JSON format and
 * provides methods to write the serialized data either to a file or to an arbitrary {@link Writer}.
 * It ensures that the output is correctly formatted and can be used for further processing or storage.
 * </p>
 *
 * @param <T> The type of {@link CrawlResult} that will be serialized. This allows the class to be used
 *            with any subclass of {@link CrawlResult}.
 */
@RequiredArgsConstructor
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
     * @throws IOException if an I/O error occurs during writing to the file.
     * @throws NullPointerException if {@code outputPath} is {@code null}.
     */
    public void saveToPath(Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "Output path cannot be null");

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            saveToWriter(writer);
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
     * @throws IOException if an I/O error occurs during writing to the writer.
     * @throws NullPointerException if {@code outputWriter} is {@code null}.
     */
    public void saveToWriter(Writer outputWriter) throws IOException {
        Objects.requireNonNull(outputWriter, "Output writer cannot be null");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(outputWriter, result);
    }

}
