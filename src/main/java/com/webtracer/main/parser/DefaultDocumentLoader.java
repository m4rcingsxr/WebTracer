package com.webtracer.main.parser;

import com.webtracer.main.ApiException;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
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

    @Override
    public Optional<Document> loadDocument(URI uri) throws ApiException {
        try {
            if (isLocalUri(uri)) {
                Path path = Path.of(uri.getPath());
                if (!Files.exists(path)) {
                    throw new ApiException("Invalid URL: Local file does not exist");
                }
                try (InputStream in = Files.newInputStream(path)) {
                    return Optional.of(Jsoup.parse(in, StandardCharsets.UTF_8.name(), ""));
                }
            } else {
                return Optional.of(Jsoup.parse(uri.toURL(), (int) parseTimeout.toMillis()));
            }
        } catch (IOException e) {
            throw new ApiException("Invalid URL", e);
        }
    }

    private boolean isLocalUri(URI uri) {
        return "file".equals(uri.getScheme());
    }
}