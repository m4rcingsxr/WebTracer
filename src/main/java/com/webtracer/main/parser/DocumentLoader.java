package com.webtracer.main.parser;

import com.webtracer.main.ApiException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Interface for loading a JSoup {@link Document} from a given {@link URI}.
 * This allows for injecting different loading strategies for testing purposes.
 */
public interface DocumentLoader {
    Optional<Document> loadDocument(URI uri) throws ApiException;
}