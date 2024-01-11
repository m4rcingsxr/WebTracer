package com.webtracer.main.parser.wordcount;

import com.webtracer.main.parser.DefaultDocumentLoader;
import com.webtracer.main.parser.DocumentLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class WordCountPageParserImplIntegrationTest {

    private DocumentLoader testDocumentLoader;
    private List<Pattern> excludePatterns;

    @BeforeEach
    void setUp() {
        excludePatterns = List.of(Pattern.compile("\\d+"));  // Example: Exclude numbers
        testDocumentLoader = new DefaultDocumentLoader(Duration.ofSeconds(2));  // 2 seconds timeout
    }

    @Test
    void givenSimpleHtml_whenParse_thenCorrectWordCountAndLinks() {
        String resourcePath = Path.of("src/test/resources/simple.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns,
                                                                     testDocumentLoader
        );

        WordCountParseResult result = parser.parse();

        assertEquals(1, result.getWordFrequencyMap().get("hello"));
        assertEquals(1, result.getWordFrequencyMap().get("world"));
        assertEquals(1, result.getHyperLinkList().size());
        assertTrue(result.getHyperLinkList().contains("https://example.com"));
    }

    @Test
    void givenComplexHtml_whenParse_thenCorrectWordCountAndLinks() {
        String resourcePath = Path.of("src/test/resources/complex.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns,
                                                                     testDocumentLoader
        );

        WordCountParseResult result = parser.parse();

        assertEquals(1, result.getWordFrequencyMap().get("this"));
        assertEquals(1, result.getWordFrequencyMap().get("is"));
        assertEquals(2, result.getWordFrequencyMap().get("a"));
        assertEquals(1, result.getWordFrequencyMap().get("complex"));
        assertEquals(2, result.getWordFrequencyMap().get("document"));
        assertEquals(1, result.getWordFrequencyMap().get("multiple"));
        assertEquals(1, result.getWordFrequencyMap().get("elements"));
        assertEquals(2, result.getHyperLinkList().size());
        assertTrue(result.getHyperLinkList().contains("https://another-example.com"));
        assertTrue(result.getHyperLinkList().contains("file://" + Path.of("src/test/resources/local-page.html").toAbsolutePath()));
    }

    @Test
    void givenEmptyHtml_whenParse_thenEmptyResult() {
        String resourcePath = Path.of("src/test/resources/empty.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns,
                                                                     testDocumentLoader
        );

        WordCountParseResult result = parser.parse();

        assertTrue(result.getWordFrequencyMap().isEmpty());
        assertTrue(result.getHyperLinkList().isEmpty());
    }

    @Test
    void givenInvalidHtml_whenParse_thenCorrectWordCount() {
        String resourcePath = Path.of("src/test/resources/invalid.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns,
                                                                     testDocumentLoader
        );

        WordCountParseResult result = parser.parse();

        assertEquals(1, result.getWordFrequencyMap().get("this"));
        assertEquals(1, result.getWordFrequencyMap().get("is"));
        assertEquals(1, result.getWordFrequencyMap().get("an"));
        assertEquals(1, result.getWordFrequencyMap().get("invalid"));
        assertEquals(1, result.getWordFrequencyMap().get("html"));
        assertEquals(1, result.getWordFrequencyMap().get("structure"));
        assertTrue(result.getHyperLinkList().isEmpty());
    }

    @Test
    void givenHtmlWithSpecialCharacters_whenParse_thenCorrectWordCountAndLinks() {
        String resourcePath = Path.of("src/test/resources/special_characters.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns, testDocumentLoader);

        WordCountParseResult result = parser.parse();

        assertEquals(1, result.getWordFrequencyMap().get("hello"));
        assertEquals(1, result.getWordFrequencyMap().get("world"));
        assertFalse(result.getWordFrequencyMap().containsKey("!"));
        assertFalse(result.getWordFrequencyMap().containsKey("@"));
        assertTrue(result.getHyperLinkList().contains("https://example.com"));
    }

    @Test
    void givenLargeHtml_whenParse_thenCorrectWordCountAndLinks() {
        String resourcePath = Path.of("src/test/resources/large.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns, testDocumentLoader);

        WordCountParseResult result = parser.parse();

        assertTrue(result.getWordFrequencyMap().size() > 1000);  // assuming large document
        assertTrue(result.getHyperLinkList().size() > 10);  // assuming multiple links
    }

    @Test
    void givenHtmlWithNestedLinks_whenParse_thenCorrectWordCountAndLinks() {
        String resourcePath = Path.of("src/test/resources/nested_links.html").toUri().toString();
        WordCountPageParserImpl parser = new WordCountPageParserImpl(resourcePath, excludePatterns, testDocumentLoader);

        WordCountParseResult result = parser.parse();

        assertEquals(4, result.getWordFrequencyMap().get("nested"));
        assertEquals(2, result.getWordFrequencyMap().get("links"));
        assertEquals(3, result.getHyperLinkList().size());
        assertTrue(result.getHyperLinkList().contains("https://nested-example.com"));
        assertTrue(result.getHyperLinkList().contains("https://deep-nested-example.com"));
    }

}
