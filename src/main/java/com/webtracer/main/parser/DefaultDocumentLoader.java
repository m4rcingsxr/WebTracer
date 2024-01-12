package com.webtracer.main.parser;

import com.webtracer.main.parser.DocumentLoader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
public final class DefaultDocumentLoader implements DocumentLoader {

    private final Duration parseTimeout;

    public DefaultDocumentLoader(Duration parseTimeout) {
        this.parseTimeout = parseTimeout;
    }

    @Override
    public Optional<Document> loadDocument(URI uri) throws IOException {
        if (isLocalUri(uri)) {
            // Convert the URI to a Path, stripping the "file://" scheme if present
            Path path = Path.of(uri.getPath());
            try (InputStream in = Files.newInputStream(path)) {
                return Optional.of(Jsoup.parse(in, StandardCharsets.UTF_8.name(), ""));
            }
        } else {
            // Apply timeout when fetching remote documents
            return Optional.of(Jsoup.parse(uri.toURL(), (int) parseTimeout.toMillis()));
        }
    }

    private boolean isLocalUri(URI uri) {
        return "file".equals(uri.getScheme());
    }
}