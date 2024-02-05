package com.webtracer.parser.wordcount;

import com.webtracer.parser.DefaultDocumentLoader;
import com.webtracer.parser.PageParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class WordCountPageParserFactoryImplTest {

    private List<Pattern> excludedPatterns;
    private Duration crawlTimeout;
    private WordCountPageParserFactoryImpl factory;

    @BeforeEach
    void setUp() {
        excludedPatterns = Collections.emptyList();
        crawlTimeout = Duration.ofSeconds(30);
        factory = new WordCountPageParserFactoryImpl(excludedPatterns, crawlTimeout);
    }

    @Test
    void givenValidUrl_whenCreatingParserInstance_thenShouldReturnNonNullParserInstance() {
        String url = "http://example.com";
        PageParser parser = factory.createParserInstance(url);

        assertNotNull(parser, "The parser instance should not be null");

        assertInstanceOf(WordCountPageParserImpl.class, parser,
                         "The parser should be an instance of WordCountPageParserImpl"
        );
    }

    @Test
    void givenNullUrl_whenCreatingParserInstance_thenShouldThrowNullPointerException() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            factory.createParserInstance(null);
        });

        assertEquals("url is marked non-null but is null", exception.getMessage(), "Expected NullPointerException with a specific message");
    }

    @Test
    void givenFactoryWithExcludedPatterns_whenCreatingParserInstance_thenShouldPassPatternsAndLoaderToParser() {
        String url = "http://example.com";
        WordCountPageParserImpl parser = (WordCountPageParserImpl) factory.createParserInstance(url);

        assertEquals(excludedPatterns, parser.getExcludeWordPatterns(), "The excluded patterns should match the ones passed to the factory");

        Assertions.assertEquals(crawlTimeout, ((DefaultDocumentLoader) parser.getDocumentLoader()).getParseTimeout(), "The document loader should have the correct crawl timeout");
    }

}