package com.webtracer.parser;

import com.webtracer.ApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("Attempting to load document from URI: {}", uri);

        try {
            if (isLocalUri(uri)) {
                log.debug("The URI {} is identified as a local file URI", uri);
                Path path = Path.of(uri.getPath());

                if (!Files.exists(path)) {
                    log.error("Local file does not exist: {}", path);
                    throw new ApiException("Invalid URL: Local file does not exist");
                }

                try (InputStream in = Files.newInputStream(path)) {
                    log.debug("Successfully loaded local file: {}", path);
                    return Optional.of(Jsoup.parse(in, StandardCharsets.UTF_8.name(), ""));
                }

            } else {
                log.debug("The URI {} is identified as a remote URL", uri);
                return Optional.of(Jsoup.parse(uri.toURL(), (int) parseTimeout.toMillis()));
            }
        } catch (IOException e) {
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
        boolean isLocal = "file".equals(uri.getScheme());
        log.debug("URI {} isLocalUri: {}", uri, isLocal);
        return isLocal;
    }
}