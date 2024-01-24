package com.webtracer.main.crawler.wordcount;

import com.webtracer.main.parser.wordcount.WordCountPageParserFactoryImpl;
import com.webtracer.main.parser.wordcount.WordCountPageParserImpl;
import com.webtracer.main.parser.wordcount.WordCountParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequentialWebCrawlerUnitTest {

    @Mock
    private Clock clock;
    @Mock
    private WordCountPageParserFactoryImpl parserFactory;
    @Mock
    private WordCountPageParserImpl pageParser;
    @Mock
    private WordCountParseResult parseResult;

    private SequentialWebCrawler crawler;

    private final Duration crawlTimeout = Duration.ofSeconds(10);
    private List<Pattern> excludedUrls;

    @BeforeEach
    void setUp() {
        int popularWordCount = 3;
        int maxDepth = 2;
        excludedUrls = new ArrayList<>();

        crawler = new SequentialWebCrawler(clock, crawlTimeout, maxDepth, popularWordCount, excludedUrls, parserFactory);
    }

    @Test
    void givenSingleUrl_whenCrawlIsCalled_thenReturnsCorrectWordCount() {
        // Given
        String url = "http://example.com";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(parserFactory.createParserInstance(url)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of("example", 1));
        when(parseResult.getHyperLinkList()).thenReturn(Collections.emptyList());

        // When
        WordCountResult result = crawler.crawl(List.of(url));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalUrlsVisited());
        assertEquals(1, result.getWordFrequencyMap().get("example"));

        // Verify
        verify(clock, times(2)).instant();
        verify(parserFactory, times(1)).createParserInstance(url);
        verify(pageParser, times(1)).parse();
    }

    @Test
    void givenDepthLimit_whenCrawlIsCalled_thenRespectsDepthLimit() {
        // Given
        String url = "http://example.com";
        String linkedUrl = "http://example.com/linked";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(parserFactory.createParserInstance(url)).thenReturn(pageParser);
        when(parserFactory.createParserInstance(linkedUrl)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of("example", 1));
        when(parseResult.getHyperLinkList()).thenReturn(List.of(linkedUrl));

        // When
        WordCountResult result = crawler.crawl(List.of(url));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalUrlsVisited());
        assertEquals(2, result.getWordFrequencyMap().get("example"));

        // Verify
        verify(clock, times(3)).instant();
        verify(parserFactory, times(1)).createParserInstance(url);
        verify(parserFactory, times(1)).createParserInstance(linkedUrl);
        verify(pageParser, times(2)).parse();
    }

    @Test
    void givenTimeout_whenCrawlIsCalled_thenStopsAtTimeout() {
        // Given
        String url = "http://example.com";
        Instant now = Instant.now();
        Instant timeout = now.plusSeconds(5);
        when(clock.instant()).thenReturn(now).thenReturn(timeout);
        when(parserFactory.createParserInstance(url)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of("example", 1));
        when(parseResult.getHyperLinkList()).thenReturn(Collections.emptyList());

        // When
        WordCountResult result = crawler.crawl(List.of(url));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalUrlsVisited());
        assertEquals(1, result.getWordFrequencyMap().get("example"));

        // Verify
        verify(clock, times(2)).instant();
        verify(parserFactory, times(1)).createParserInstance(url);
        verify(pageParser, times(1)).parse();
    }

    @Test
    void givenExclusionPattern_whenCrawlIsCalled_thenExcludesMatchingUrls() {
        // Given
        String url = "http://example.com";
        excludedUrls.add(Pattern.compile(".*example.com.*"));
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        // When
        WordCountResult result = crawler.crawl(List.of(url));

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().isEmpty());

        // Verify
        verify(clock, times(2)).instant();
        verify(parserFactory, times(0)).createParserInstance(url);
        verify(pageParser, times(0)).parse();
    }

    @Test
    void givenVisitedUrls_whenCrawlIsCalled_thenSkipsAlreadyVisitedUrls() {
        // Given
        String url = "http://example.com";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(parserFactory.createParserInstance(url)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of("example", 1));
        when(parseResult.getHyperLinkList()).thenReturn(List.of(url));

        // When
        WordCountResult result = crawler.crawl(List.of(url));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalUrlsVisited());
        assertEquals(1, result.getWordFrequencyMap().get("example"));

        // Verify
        verify(clock, times(3)).instant();
        verify(parserFactory, times(1)).createParserInstance(url);
        verify(pageParser, times(1)).parse();
    }

    @Test
    void givenEmptyStartingUrls_whenCrawlIsCalled_thenReturnsEmptyResult() {
        // Given
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        // When
        WordCountResult result = crawler.crawl(Collections.emptyList());

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().isEmpty());

        // Verify
        verify(clock, times(1)).instant();
        verify(parserFactory, times(0)).createParserInstance(anyString());
        verify(pageParser, times(0)).parse();
    }

    @Test
    void givenMultipleStartingUrls_whenCrawlIsCalled_thenAggregatesWordCounts() {
        // Given
        String url1 = "http://example.com";
        String url2 = "http://example.org";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(parserFactory.createParserInstance(url1)).thenReturn(pageParser);
        when(parserFactory.createParserInstance(url2)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of("example", 1));
        when(parseResult.getHyperLinkList()).thenReturn(Collections.emptyList());

        // When
        WordCountResult result = crawler.crawl(List.of(url1, url2));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalUrlsVisited());
        assertEquals(2, result.getWordFrequencyMap().get("example"));

        // Verify
        verify(clock, times(3)).instant();
        verify(parserFactory, times(1)).createParserInstance(url1);
        verify(parserFactory, times(1)).createParserInstance(url2);
        verify(pageParser, times(2)).parse();
    }

    @Test
    void givenMultiplePages_whenCrawlIsCalled_thenAggregatesWordCountsAcrossPages() {
        // Given
        String url1 = "http://example.com";
        String url2 = "http://example.org";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(parserFactory.createParserInstance(url1)).thenReturn(pageParser);
        when(parserFactory.createParserInstance(url2)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of("example", 1));
        when(parseResult.getHyperLinkList()).thenReturn(Collections.emptyList());

        // When
        WordCountResult result = crawler.crawl(List.of(url1, url2));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalUrlsVisited());
        assertEquals(2, result.getWordFrequencyMap().get("example"));

        // Verify
        verify(clock, times(3)).instant();
        verify(parserFactory, times(1)).createParserInstance(url1);
        verify(parserFactory, times(1)).createParserInstance(url2);
        verify(pageParser, times(2)).parse();
    }

    @Test
    void givenPopularWordCountLimit_whenCrawlIsCalled_thenLimitsResultToPopularWords() {
        // Given
        String url = "http://example.com";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(parserFactory.createParserInstance(url)).thenReturn(pageParser);
        when(pageParser.parse()).thenReturn(parseResult);
        when(parseResult.getWordFrequencyMap()).thenReturn(Map.of(
                "example", 5,
                "test", 3,
                "sample", 2,
                "another", 1
        ));
        when(parseResult.getHyperLinkList()).thenReturn(Collections.emptyList());

        // When
        WordCountResult result = crawler.crawl(List.of(url));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalUrlsVisited());
        Map<String, Integer> expected = Map.of(
                "example", 5,
                "test", 3,
                "sample", 2
        );
        assertEquals(expected, result.getWordFrequencyMap());

        // Verify
        verify(clock, times(2)).instant();
        verify(parserFactory, times(1)).createParserInstance(url);
        verify(pageParser, times(1)).parse();
    }
}
