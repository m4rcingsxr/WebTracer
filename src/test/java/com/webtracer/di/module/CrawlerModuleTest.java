package com.webtracer.di.module;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.DomainThrottler;
import com.webtracer.crawler.GenericWebCrawler;
import com.webtracer.crawler.wordcount.SequentialWebCrawler;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.wordcount.WordCountPageParserFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CrawlerModuleTest {

    private WebCrawlerConfig config;
    private Injector injector;

    @BeforeEach
    void setUp() {
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        config = WebCrawlerConfig.builder()
                .initialPages(Collections.singletonList(resourcePath))
                .excludedUrls(Collections.singletonList(Pattern.compile(".*excluded.*")))
                .customImplementation(SequentialWebCrawler.class.getName())
                .maxDepth(3)
                .timeout(Duration.ofSeconds(10))
                .popularWordCount(5)
                .concurrencyLevel(4)
                .throttleDelayMillis(2000) // Adding the new throttle delay configuration
                .build();

        injector = Guice.createInjector(new CrawlerModule(config));
    }

    @Test
    void givenSequentialWebCrawler_whenInjected_thenShouldBeInstanceOfSequentialWebCrawler() {
        GenericWebCrawler crawler = injector.getInstance(Key.get(GenericWebCrawler.class));

        // Check that the injected crawler is an instance of SequentialWebCrawler
        assertInstanceOf(SequentialWebCrawler.class, crawler);

        // Verify that the crawler works as expected
        assertDoesNotThrow(() -> {
            var result = crawler.crawl(config.getInitialPages());
            assertNotNull(result);
            assertTrue(result.getTotalUrlsVisited() > 0);
        });
    }

    @Test
    void givenCrawlerModule_whenWordCountFactoryIsInjected_thenShouldProvideWordCountPageParserFactoryImpl() throws NoSuchFieldException, IllegalAccessException {
        GenericWebCrawler crawler = injector.getInstance(Key.get(GenericWebCrawler.class));

        // Use reflection to access the private field `parserFactory` in SequentialWebCrawler
        Field factoryField = SequentialWebCrawler.class.getDeclaredField("parserFactory");
        factoryField.setAccessible(true);
        AbstractPageParserFactory factory = (AbstractPageParserFactory) factoryField.get(crawler);

        // Verify the factory is not null and is of the correct type
        assertNotNull(factory);
        assertInstanceOf(WordCountPageParserFactoryImpl.class, factory);

        // Verify that the factory can create a parser instance without throwing an exception
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        assertDoesNotThrow(() -> factory.createParserInstance(resourcePath));

        // Verify the crawler works as expected with the injected parser factory
        assertDoesNotThrow(() -> {
            var result = crawler.crawl(config.getInitialPages());
            assertNotNull(result);
            assertTrue(result.getTotalUrlsVisited() > 0);
        });
    }

    @Test
    void givenValidCustomImplementationInConfig_whenInjected_thenShouldReturnCorrectCrawlerInstance() {
        config = WebCrawlerConfig.builder()
                .customImplementation(SequentialWebCrawler.class.getName())
                .initialPages(config.getInitialPages())
                .excludedUrls(config.getExcludedUrls())
                .maxDepth(config.getMaxDepth())
                .timeout(config.getTimeout())
                .popularWordCount(config.getPopularWordCount())
                .concurrencyLevel(config.getConcurrencyLevel())
                .throttleDelayMillis(config.getThrottleDelayMillis()) // Ensure the throttle delay is passed
                .build();

        injector = Guice.createInjector(new CrawlerModule(config));
        GenericWebCrawler crawler = injector.getInstance(Key.get(GenericWebCrawler.class));

        // Verify the correct crawler instance is returned
        assertNotNull(crawler);
        assertInstanceOf(SequentialWebCrawler.class, crawler);

        // Verify the crawler works as expected
        assertDoesNotThrow(() -> {
            var result = crawler.crawl(config.getInitialPages());
            assertNotNull(result);
            assertTrue(result.getTotalUrlsVisited() > 0);
        });
    }

    @Test
    void givenInvalidCustomImplementationInConfig_whenInjected_thenShouldThrowProvisionException() {
        config = WebCrawlerConfig.builder()
                .customImplementation("InvalidClassName")
                .initialPages(config.getInitialPages())
                .excludedUrls(config.getExcludedUrls())
                .maxDepth(config.getMaxDepth())
                .timeout(config.getTimeout())
                .popularWordCount(config.getPopularWordCount())
                .concurrencyLevel(config.getConcurrencyLevel())
                .throttleDelayMillis(config.getThrottleDelayMillis()) // Ensure the throttle delay is passed
                .build();

        injector = Guice.createInjector(new CrawlerModule(config));

        // Expect a ProvisionException due to the invalid class name
        assertThrows(ProvisionException.class, () -> injector.getInstance(GenericWebCrawler.class));
    }

    @Test
    void givenThrottleDelay_whenInjected_thenDomainThrottlerShouldUseCorrectDelay() throws NoSuchFieldException, IllegalAccessException {
        DomainThrottler throttler = injector.getInstance(DomainThrottler.class);

        // Use reflection to access the private field `delayBetweenRequests` in DomainThrottler
        Field delayField = DomainThrottler.class.getDeclaredField("delayBetweenRequests");
        delayField.setAccessible(true);
        long delayBetweenRequests = (long) delayField.get(throttler);

        // Verify the delay is correctly set
        assertEquals(config.getThrottleDelayMillis(), delayBetweenRequests);
    }
}
