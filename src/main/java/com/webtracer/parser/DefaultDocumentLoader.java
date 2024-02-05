package com.webtracer.parser;

import com.webtracer.ApiException;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/**
 * Default implementation of {@link DocumentLoader} that handles both local and remote URIs.
 * It applies a timeout for remote documents.
 */
@Getter
public final class DefaultDocumentLoader implements DocumentLoader {

    private final Duration parseTimeout;

    public DefaultDocumentLoader(Duration parseTimeout) {
        this.parseTimeout = parseTimeout;
    }

    /**
     * Loads a JSoup {@link Document} from the given {@link URI}.
     *
     * This method attempts to retrieve and parse the document located at the specified URI.
     * It handles both local files and remote URLs. If the document cannot be loaded for any reason,
     * an {@link ApiException} is thrown. The exception is propagated up the call stack and should be
     * handled by the calling layer.
     *
     * @param uri the {@link URI} of the document to be loaded.
     * @return an {@link Optional<Document>} containing the parsed document if successful.
     * @throws ApiException if the document cannot be loaded due to an error (e.g., I/O error, invalid URI).
     */
    @Override
    public Optional<Document> loadDocument(URI uri) throws ApiException {
        try {
            // Check if the URI points to a local file
            if (isLocalUri(uri)) {
                // Convert the URI to a Path object
                Path path = Path.of(uri.getPath());

                // Check if the local file exists
                if (!Files.exists(path)) {
                    // If the file doesn't exist, throw an ApiException
                    throw new ApiException("Invalid URL: Local file does not exist");
                }

                // Open an InputStream to the file and parse it into a JSoup Document
                try (InputStream in = Files.newInputStream(path)) {
                    return Optional.of(Jsoup.parse(in, StandardCharsets.UTF_8.name(), ""));
                }

            } else {
                // If the URI is remote, use JSoup to parse the document with a timeout
                return Optional.of(Jsoup.parse(uri.toURL(), (int) parseTimeout.toMillis()));
            }
        } catch (IOException e) {
            // If an IOException occurs (e.g., network error, file I/O error), throw an ApiException
            throw new ApiException("Invalid URL", e);
        }
    }

    /**
     * Checks whether the given URI is a local file URI.
     *
     * @param uri the {@link URI} to check.
     * @return {@code true} if the URI is a local file URI, {@code false} otherwise.
     */
    private boolean isLocalUri(URI uri) {
        return "file".equals(uri.getScheme());
    }
}