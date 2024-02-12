package com.webtracer.crawler.wordcount;

import com.webtracer.ApiException;
import com.webtracer.crawler.DomainThrottler;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.wordcount.WordCountPageParserImpl;
import com.webtracer.parser.wordcount.WordCountParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RecursiveActionWebCrawlerTest {

    private RecursiveActionWebCrawler crawler;
    private AbstractPageParserFactory parserFactory;
    private Clock clock;
    private Duration crawlTimeout;
    private int topWordCount;
    private int concurrencyLevel;
    private int maximumDepth;
    private List<Pattern> excludedUrls;
    private List<String> initialPages;
    private DomainThrottler domainThrottler;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        parserFactory = mock(AbstractPageParserFactory.class);
        crawlTimeout = Duration.ofSeconds(10);
        topWordCount = 5;
        concurrencyLevel = 4;
        maximumDepth = 3;
        excludedUrls = List.of(Pattern.compile(".*exclude.*"));
        initialPages = List.of("http://example.com");
        domainThrottler = mock(DomainThrottler.class);

        crawler = new RecursiveActionWebCrawler(
                clock,
                parserFactory,
                crawlTimeout,
                topWordCount,
                concurrencyLevel,
                maximumDepth,
                excludedUrls,
                domainThrottler
        );
    }

    @Test
    void givenValidConfiguration_whenCrawling_thenShouldReturnCorrectWordCountResult() throws ApiException, InterruptedException {
        Instant fixedInstant = Instant.now();
        when(clock.instant()).thenReturn(fixedInstant);

        WordCountParseResult mockParseResult = new WordCountParseResult.Builder()
                .addWord("test")
                .addLink("http://example.com/page1")
                .build();

        when(parserFactory.createParserInstance(anyString())).thenReturn(mock(WordCountPageParserImpl.class));
        when(((WordCountPageParserImpl) parserFactory.createParserInstance(anyString())).parse()).thenReturn(mockParseResult);

        WordCountResult result = crawler.crawl(initialPages);

        assertNotNull(result);
        assertEquals(2, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().containsKey("test"));
        assertEquals(2, result.getWordFrequencyMap().get("test"));

        verify(parserFactory).createParserInstance("http://example.com");
        verify(domainThrottler, times(2)).acquire(anyString());
    }

    @Test
    void givenCrawlTimeout_whenCrawling_thenShouldRespectTimeout() throws ApiException, InterruptedException {
        Instant startInstant = Instant.now();
        Instant timeoutInstant = startInstant.plus(crawlTimeout).plusSeconds(1);
        when(clock.instant()).thenReturn(startInstant, timeoutInstant);

        WordCountResult result = crawler.crawl(initialPages);

        assertNotNull(result);
        assertEquals(0, result.getTotalUrlsVisited());

        verify(parserFactory, never()).createParserInstance(anyString());
        verify(domainThrottler, never()).acquire(anyString());
    }

    @Test
    void givenMaximumDepth_whenCrawling_thenShouldNotExceedDepth() throws ApiException, InterruptedException {
        Instant fixedInstant = Instant.now();
        when(clock.instant()).thenReturn(fixedInstant);

        WordCountParseResult mockParseResult = new WordCountParseResult.Builder()
                .addWord("test")
                .addLink("http://example.com/page1")
                .build();

        when(parserFactory.createParserInstance(anyString())).thenReturn(mock(WordCountPageParserImpl.class));
        when(((WordCountPageParserImpl) parserFactory.createParserInstance(anyString())).parse()).thenReturn(mockParseResult);

        crawler = new RecursiveActionWebCrawler(
                clock,
                parserFactory,
                crawlTimeout,
                topWordCount,
                concurrencyLevel,
                1,  // max depth set to 1
                excludedUrls,
                domainThrottler
        );
        WordCountResult result = crawler.crawl(initialPages);

        assertNotNull(result);
        assertEquals(1, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().containsKey("test"));
        assertEquals(1, result.getWordFrequencyMap().get("test"));

        verify(parserFactory, times(1)).createParserInstance("http://example.com");
        verify(domainThrottler, times(1)).acquire(anyString());
    }

    @Test
    void givenExcludedUrls_whenCrawling_thenShouldNotVisitExcludedUrls() throws ApiException, InterruptedException {
        Instant fixedInstant = Instant.now();
        when(clock.instant()).thenReturn(fixedInstant);

        WordCountParseResult mockParseResult = new WordCountParseResult.Builder()
                .addWord("test")
                .addLink("http://example.com/exclude-this")
                .build();

        when(parserFactory.createParserInstance(anyString())).thenReturn(mock(WordCountPageParserImpl.class));
        when(((WordCountPageParserImpl) parserFactory.createParserInstance(anyString())).parse()).thenReturn(mockParseResult);

        WordCountResult result = crawler.crawl(initialPages);

        assertNotNull(result);
        assertEquals(1, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().containsKey("test"));
        assertFalse(result.getWordFrequencyMap().containsKey("exclude-this"));

        verify(parserFactory, never()).createParserInstance("http://example.com/exclude-this");
        verify(domainThrottler, times(1)).acquire(anyString());
    }

    @Test
    void givenValidConfiguration_whenCrawling_thenShouldNotThrowExceptions() throws InterruptedException {
        Instant fixedInstant = Instant.now();
        when(clock.instant()).thenReturn(fixedInstant);

        WordCountParseResult mockParseResult = new WordCountParseResult.Builder()
                .addWord("test")
                .build();

        when(parserFactory.createParserInstance(anyString())).thenReturn(mock(WordCountPageParserImpl.class));
        when(((WordCountPageParserImpl) parserFactory.createParserInstance(anyString())).parse()).thenReturn(mockParseResult);

        assertDoesNotThrow(() -> crawler.crawl(initialPages));

        verify(parserFactory, times(2)).createParserInstance(anyString());
        verify(domainThrottler, times(1)).acquire(anyString());
    }
}
