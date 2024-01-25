package com.webtracer.main.crawler.wordcount;

import com.webtracer.main.parser.wordcount.WordCountPageParserFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SequentialWebCrawlerIntegrationTest {

    private SequentialWebCrawler webCrawler;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final Duration crawlTimeout = Duration.ofSeconds(2);

    @BeforeEach
    void setUp() {
        List<Pattern> excludedUrls = List.of();
        WordCountPageParserFactoryImpl parserFactory = new WordCountPageParserFactoryImpl(excludedUrls, crawlTimeout);
        webCrawler = new SequentialWebCrawler(clock, crawlTimeout, 10, 10, excludedUrls, parserFactory);
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
        webCrawler = new SequentialWebCrawler(clock, Duration.ofSeconds(1), 10, 10, List.of(), new WordCountPageParserFactoryImpl(List.of(), Duration.ofSeconds(1)));
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
        webCrawler = new SequentialWebCrawler(clock, Duration.ofSeconds(10), 1, 10, List.of(), new WordCountPageParserFactoryImpl(List.of(), Duration.ofSeconds(10)));
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(1, result.getTotalUrlsVisited());

        assertTrue(result.getWordFrequencyMap().containsKey("section"));
        assertTrue(result.getWordFrequencyMap().containsKey("index"));
        assertTrue(result.getWordFrequencyMap().containsKey("main"));
        assertTrue(result.getWordFrequencyMap().containsKey("to"));
        assertTrue(result.getWordFrequencyMap().containsKey("provides"));
        assertTrue(result.getWordFrequencyMap().containsKey("sections"));
        assertTrue(result.getWordFrequencyMap().containsKey("various"));
        assertTrue(result.getWordFrequencyMap().containsKey("welcome"));
        assertTrue(result.getWordFrequencyMap().containsKey("links"));
        assertTrue(result.getWordFrequencyMap().containsKey("home"));

        assertEquals(2, result.getWordFrequencyMap().get("section"));
        assertEquals(2, result.getWordFrequencyMap().get("index"));
        assertEquals(2, result.getWordFrequencyMap().get("main"));
        assertEquals(2, result.getWordFrequencyMap().get("to"));
        assertEquals(1, result.getWordFrequencyMap().get("provides"));
        assertEquals(1, result.getWordFrequencyMap().get("sections"));
        assertEquals(1, result.getWordFrequencyMap().get("various"));
        assertEquals(1, result.getWordFrequencyMap().get("welcome"));
        assertEquals(1, result.getWordFrequencyMap().get("links"));
        assertEquals(1, result.getWordFrequencyMap().get("home"));

        assertFalse(result.getWordFrequencyMap().containsKey("Subsection"));
    }

    @Test
    void whenCrawlingWithExclusionPattern_thenShouldNotVisitExcludedUrls() {
        Pattern excludePattern = Pattern.compile(".*subsection1a\\.html.*");
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        webCrawler = new SequentialWebCrawler(clock, Duration.ofSeconds(10), 10, 10, List.of(excludePattern), new WordCountPageParserFactoryImpl(List.of(excludePattern), Duration.ofSeconds(10)));
        List<String> startingUrls = List.of(resourcePath);
        WordCountResult result = webCrawler.crawl(startingUrls);

        assertEquals(4, result.getTotalUrlsVisited());
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
    void whenCrawlingNonExistentUrl_thenNoUrlsShouldBeVisited() {
        String resourcePath = Path.of("src/test/resources/nonexistent.html").toUri().toString();
        List<String> startingUrls = List.of(resourcePath);
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
