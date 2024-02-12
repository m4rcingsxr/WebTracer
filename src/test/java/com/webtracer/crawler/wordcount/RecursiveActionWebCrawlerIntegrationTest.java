package com.webtracer.crawler.wordcount;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.di.module.CrawlerModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveActionWebCrawlerIntegrationTest {

    private RecursiveActionWebCrawler webCrawler;
    private Injector injector;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final Duration crawlTimeout = Duration.ofSeconds(2);

    @BeforeEach
    void setUp() {
        WebCrawlerConfig config = WebCrawlerConfig.builder()
                .maxDepth(10)
                .popularWordCount(10)
                .timeout(crawlTimeout)
                .excludedUrls(List.of())
                .concurrencyLevel(10)
                .throttleDelayMillis(0)
                .build();

        // Inject dependencies using Guice
        injector = Guice.createInjector(new CrawlerModule(config));
        webCrawler = injector.getInstance(RecursiveActionWebCrawler.class);
    }

    @Test
    void whenCrawlingStartingFromIndex_thenAllPagesShouldBeVisited() {
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(5, result.getTotalUrlsVisited());

        assertTrue(result.getWordFrequencyMap().containsKey("section"));
        assertTrue(result.getWordFrequencyMap().containsKey("subsection"));
        assertTrue(result.getWordFrequencyMap().containsKey("1a"));
        assertTrue(result.getWordFrequencyMap().containsKey("1b"));
        assertTrue(result.getWordFrequencyMap().containsKey("topic"));
        assertTrue(result.getWordFrequencyMap().containsKey("2"));

        assertEquals(11, result.getWordFrequencyMap().get("section"));
        assertEquals(6, result.getWordFrequencyMap().get("subsection"));
        assertEquals(6, result.getWordFrequencyMap().get("1"));
        assertEquals(4, result.getWordFrequencyMap().get("topic"));
        assertEquals(4, result.getWordFrequencyMap().get("1a"));
        assertEquals(4, result.getWordFrequencyMap().get("1b"));
        assertEquals(4, result.getWordFrequencyMap().get("2"));
    }

    @Test
    void whenCrawlingWithTimeout_thenCrawlShouldStopAfterTimeout() {
        String resourcePath = Path.of("src/test/resources/loop.html").toUri().toString();
        webCrawler = injector.getInstance(RecursiveActionWebCrawler.class);
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(1, result.getTotalUrlsVisited());

        assertTrue(result.getWordFrequencyMap().containsKey("loop"));
        assertTrue(result.getWordFrequencyMap().containsKey("page"));
        assertTrue(result.getWordFrequencyMap().containsKey("infinite"));
        assertTrue(result.getWordFrequencyMap().containsKey("test"));
        assertTrue(result.getWordFrequencyMap().containsKey("this"));
        assertTrue(result.getWordFrequencyMap().containsKey("of"));
        assertTrue(result.getWordFrequencyMap().containsKey("an"));
        assertTrue(result.getWordFrequencyMap().containsKey("is"));

        assertEquals(3, result.getWordFrequencyMap().get("loop"));
        assertEquals(3, result.getWordFrequencyMap().get("page"));
        assertEquals(2, result.getWordFrequencyMap().get("infinite"));
        assertEquals(2, result.getWordFrequencyMap().get("test"));
        assertEquals(2, result.getWordFrequencyMap().get("this"));
        assertEquals(1, result.getWordFrequencyMap().get("of"));
        assertEquals(1, result.getWordFrequencyMap().get("an"));
        assertEquals(1, result.getWordFrequencyMap().get("is"));
    }

    @Test
    void whenCrawlingWithDepthLimit_thenShouldOnlyVisitUpToDepthLimit() {
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        webCrawler = injector.getInstance(RecursiveActionWebCrawler.class);
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(5, result.getTotalUrlsVisited());

        assertTrue(result.getWordFrequencyMap().containsKey("section"));
        assertTrue(result.getWordFrequencyMap().containsKey("subsection"));
        assertTrue(result.getWordFrequencyMap().containsKey("to"));
        assertTrue(result.getWordFrequencyMap().containsKey("about"));
        assertTrue(result.getWordFrequencyMap().containsKey("topic"));
        assertTrue(result.getWordFrequencyMap().containsKey("this"));
        assertTrue(result.getWordFrequencyMap().containsKey("1a"));
        assertTrue(result.getWordFrequencyMap().containsKey("1b"));
        assertTrue(result.getWordFrequencyMap().containsKey("2"));

        assertEquals(11, result.getWordFrequencyMap().get("section"));
        assertEquals(6, result.getWordFrequencyMap().get("subsection"));
        assertEquals(6, result.getWordFrequencyMap().get("to"));
        assertEquals(4, result.getWordFrequencyMap().get("about"));
        assertEquals(4, result.getWordFrequencyMap().get("topic"));
        assertEquals(4, result.getWordFrequencyMap().get("this"));
        assertEquals(4, result.getWordFrequencyMap().get("1a"));
        assertEquals(4, result.getWordFrequencyMap().get("1b"));
        assertEquals(4, result.getWordFrequencyMap().get("2"));
    }

    @Test
    void whenCrawlingWithExclusionPattern_thenShouldNotVisitExcludedUrls() {
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        webCrawler = injector.getInstance(RecursiveActionWebCrawler.class);
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(5, result.getTotalUrlsVisited());
        assertFalse(result.getWordFrequencyMap().containsKey("1A"));
    }

    @Test
    void whenCrawlingWithMultipleStartingUrls_thenAllStartingPointsShouldBeCrawled() {
        String resourcePath1 = Path.of("src/test/resources/section1.html").toUri().toString();
        String resourcePath2 = Path.of("src/test/resources/section2.html").toUri().toString();
        List<String> startingUrls = List.of(resourcePath1, resourcePath2);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(5, result.getTotalUrlsVisited());

        assertTrue(result.getWordFrequencyMap().containsKey("section"));
        assertTrue(result.getWordFrequencyMap().containsKey("subsection"));
        assertTrue(result.getWordFrequencyMap().containsKey("1a"));
        assertTrue(result.getWordFrequencyMap().containsKey("1b"));
        assertTrue(result.getWordFrequencyMap().containsKey("topic"));
        assertTrue(result.getWordFrequencyMap().containsKey("to"));

        assertEquals(11, result.getWordFrequencyMap().get("section"));
        assertEquals(6, result.getWordFrequencyMap().get("subsection"));
        assertEquals(6, result.getWordFrequencyMap().get("1"));
        assertEquals(6, result.getWordFrequencyMap().get("to"));
        assertEquals(4, result.getWordFrequencyMap().get("about"));
        assertEquals(4, result.getWordFrequencyMap().get("topic"));
        assertEquals(4, result.getWordFrequencyMap().get("this"));
        assertEquals(4, result.getWordFrequencyMap().get("1a"));
        assertEquals(4, result.getWordFrequencyMap().get("1b"));
        assertEquals(4, result.getWordFrequencyMap().get("2"));
    }

    @Test
    void whenCrawlingWithEmptyStartUrls_thenNoUrlsShouldBeVisited() {
        List<String> startingUrls = List.of();
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(0, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().isEmpty());
    }

    @Test
    void whenCrawlingPageWithNoContent_thenShouldHandleEmptyPageGracefully() {
        String resourcePath = Path.of("src/test/resources/empty.html").toUri().toString();
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(1, result.getTotalUrlsVisited());
        assertTrue(result.getWordFrequencyMap().isEmpty());
    }
}
