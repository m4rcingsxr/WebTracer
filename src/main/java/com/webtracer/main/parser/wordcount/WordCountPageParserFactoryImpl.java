package com.webtracer.main.parser.wordcount;

import com.webtracer.main.parser.AbstractPageParserFactory;
import com.webtracer.main.parser.DefaultDocumentLoader;
import com.webtracer.main.parser.DocumentLoader;
import com.webtracer.main.parser.PageParser;
import lombok.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code WordCountPageParserFactoryImpl} class is a concrete implementation of the {@link AbstractPageParserFactory}
 * interface, responsible for creating instances of {@link WordCountPageParserImpl}. This factory specifically handles
 * the creation of parsers that count words on web pages while excluding certain patterns from being processed.
 *
 * <p>The factory is initialized with a list of {@link Pattern} objects that define the URLs or content patterns to be
 * excluded from the parsing process. Additionally, it uses a {@link DocumentLoader} to fetch and load the documents
 * (web pages) that will be parsed.
 *
 * <p>This implementation leverages the Abstract Factory pattern, allowing the creation of specialized parser instances
 * without tying the client code to the specifics of the parser's construction.
 */
public final class WordCountPageParserFactoryImpl implements AbstractPageParserFactory {

    private final List<Pattern> excludedPatterns;
    private final DocumentLoader documentLoader;

    /**
     * Constructs a {@code WordCountPageParserFactoryImpl} with the specified exclusion patterns and crawl timeout.
     * The {@link DefaultDocumentLoader} is used internally to handle the document loading process.
     *
     * @param excludedPatterns a list of {@link Pattern} objects representing the URL patterns or content patterns
     *                         that should be excluded from parsing.
     * @param crawlTimeout     the duration to be used as a timeout for the document loading process.
     */
    public WordCountPageParserFactoryImpl(
            List<Pattern> excludedPatterns,
            Duration crawlTimeout) {
        this.excludedPatterns = excludedPatterns;
        this.documentLoader = new DefaultDocumentLoader(crawlTimeout);
    }

    /**
     * Creates an instance of {@link WordCountPageParserImpl}, configured with the specified URL, exclusion patterns,
     * and document loader. This method provides a fully initialized parser ready to perform word counting on the
     * specified URL.
     *
     * @param url the URL of the web page to be parsed.
     * @return a new instance of {@link WordCountPageParserImpl}, configured with the provided URL, exclusion patterns,
     *         and document loader.
     * @throws NullPointerException if the {@code url} is {@code null}.
     */
    @Override
    public WordCountPageParserImpl createParserInstance(@NonNull final String url) {
        return new WordCountPageParserImpl(url, excludedPatterns, documentLoader);
    }

}
