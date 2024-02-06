package com.webtracer.di.module;

import com.google.inject.*;
import com.webtracer.config.WebCrawlerConfig;
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
                .build();

        injector = Guice.createInjector(new CrawlerModule(config));
    }

    @Test
    void givenSequentialWebCrawler_whenInjected_thenShouldBeInstanceOfSequentialWebCrawler() {
        GenericWebCrawler crawler = injector.getInstance(
                Key.get(GenericWebCrawler.class));

        assertInstanceOf(SequentialWebCrawler.class, crawler);

        assertDoesNotThrow(() -> {
            var result = crawler.crawl(config.getInitialPages());
            assertNotNull(result);
            assertTrue(result.getTotalUrlsVisited() > 0);
        });
    }

    @Test
    void givenCrawlerModule_whenWordCountFactoryIsInjected_thenShouldProvideWordCountPageParserFactoryImpl() throws NoSuchFieldException, IllegalAccessException {
        GenericWebCrawler crawler = injector.getInstance(
                Key.get(GenericWebCrawler.class));

        Field factoryField = SequentialWebCrawler.class.getDeclaredField("parserFactory");
        factoryField.setAccessible(true);
        AbstractPageParserFactory factory = (AbstractPageParserFactory) factoryField.get(crawler);

        assertNotNull(factory);
        assertInstanceOf(WordCountPageParserFactoryImpl.class, factory);

        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        assertDoesNotThrow(() -> factory.createParserInstance(resourcePath));

        assertDoesNotThrow(() -> {
            var result = crawler.crawl(config.getInitialPages());
            assertNotNull(result);
            assertTrue(result.getTotalUrlsVisited() > 0);
        });
    }

    @Test
    void givenValidCustomImplementationInConfig_whenInjected_thenShouldReturnCorrectCrawlerInstance() {
        String resourcePath = Path.of("src/test/resources/index.html").toUri().toString();
        config = WebCrawlerConfig.builder()
                .customImplementation(SequentialWebCrawler.class.getName())
                .initialPages(Collections.singletonList(resourcePath))
                .excludedUrls(config.getExcludedUrls())
                .maxDepth(config.getMaxDepth())
                .timeout(config.getTimeout())
                .popularWordCount(config.getPopularWordCount())
                .build();

        injector = Guice.createInjector(new CrawlerModule(config));
        GenericWebCrawler crawler = injector.getInstance(
                Key.get(GenericWebCrawler.class));

        assertNotNull(crawler);
        assertInstanceOf(SequentialWebCrawler.class, crawler);

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
                .build();

        injector = Guice.createInjector(new CrawlerModule(config));

        assertThrows(ProvisionException.class, () -> injector.getInstance(GenericWebCrawler.class));
    }
}
