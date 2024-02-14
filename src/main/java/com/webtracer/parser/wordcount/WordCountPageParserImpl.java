package com.webtracer.parser.wordcount;

import com.webtracer.ApiException;
import com.webtracer.parser.DocumentLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WordCountPageParser} that can handle both local and remote
 * HTML files.
 *
 * <p> Utilizes the JSoup library for HTML parsing. This class acts as an adapter around JSoup's
 * API, as JSoup does not resolve relative hyperlinks correctly when parsing local HTML files.</p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public final class WordCountPageParserImpl implements WordCountPageParser {

    @NonNull
    private final String pageUri;

    @NonNull
    private final List<Pattern> excludeWordPatterns;

    @NonNull
    private final DocumentLoader documentLoader;

    /**
     * Parses the HTML page specified by {@code pageUri} and returns a {@link WordCountParseResult}
     * containing the word frequencies and hyperlinks.
     *
     * @return the result of parsing the HTML page, including word frequencies and hyperlinks
     */
    @Override
    public WordCountParseResult parse() {
        log.debug("Starting to parse the page: {}", pageUri);

        Optional<URI> uriOpt = parseURI(pageUri);
        if (uriOpt.isEmpty()) {
            log.warn("Failed to parse URI: {}", pageUri);
            return new WordCountParseResult.Builder().build();
        }

        URI uri = uriOpt.get();
        log.debug("Parsed URI: {}", uri);

        Optional<Document> documentOpt;

        try {
            documentOpt = documentLoader.loadDocument(uri);
        } catch (ApiException e) {
            log.warn("Failed to load document: {}", uri, e);
            return new WordCountParseResult.Builder().build();
        }

        if (documentOpt.isEmpty()) {
            log.warn("Failed to load document from URI: {}", uri);
            return new WordCountParseResult.Builder().build();
        }

        Document document = documentOpt.get();
        log.debug("Loaded document from URI: {}", uri);

        WordCountParseResult.Builder resultBuilder = new WordCountParseResult.Builder();
        WordCountNodeProcessor nodeProcessor = new WordCountNodeProcessor(excludeWordPatterns, resultBuilder, uri);

        // Traverse the document and process each node, builder accessed by single thread
        document.traverse(nodeProcessor::processNode);

        log.debug("Finished parsing the page: {}", pageUri);
        return nodeProcessor.getResult();
    }

    /**
     * Converts the given string to a {@link URI}.
     *
     * @param uriString the string to be converted
     * @return an {@link Optional} containing the {@link URI} if the string is a valid URI,
     *         or an empty {@link Optional} if the string is not a valid URI
     */
    private Optional<URI> parseURI(String uriString) {
        try {
            URI uri = new URI(uriString);
            log.debug("Successfully parsed URI: {}", uriString);
            return Optional.of(uri);
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI: {}", uriString, e);
            return Optional.empty();
        }
    }
}
