package com.webtracer.di.module;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.webtracer.ApiException;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.GenericWebCrawler;
import com.webtracer.crawler.wordcount.RecursiveActionWebCrawler;
import com.webtracer.crawler.wordcount.SequentialWebCrawler;
import com.webtracer.di.annotation.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class CrawlerModule extends AbstractModule {

    @NonNull
    private final WebCrawlerConfig config;

    @Override
    protected void configure() {
        log.debug("Configuring CrawlerModule with WebCrawlerConfig: {}", config);

        Multibinder<GenericWebCrawler> multibinder =
                Multibinder.newSetBinder(binder(), GenericWebCrawler.class);
        multibinder.addBinding().to(SequentialWebCrawler.class);
        multibinder.addBinding().to(RecursiveActionWebCrawler.class);

        bind(Clock.class).toInstance(Clock.systemUTC());
        bind(Key.get(Integer.class, CrawlMaxDepth.class)).toInstance(config.getMaxDepth());
        bind(Key.get(Integer.class, PopularWordCount.class)).toInstance(config.getPopularWordCount());
        bind(Key.get(Duration.class, CrawlTimeout.class)).toInstance(config.getTimeout());
        bind(new Key<List<Pattern>>(ExcludedUrls.class) {}).toInstance(config.getExcludedUrls());

        install(
                ParserModule.builder()
                        .excludedWords(config.getExcludedWords())
                        .crawlTimeout(config.getTimeout())
                        .build()
        );

        log.info("CrawlerModule configuration complete");
    }

    @Provides
    @Singleton
    GenericWebCrawler provideWebCrawler(Set<GenericWebCrawler> implementations, @ConcurrencyLevel int parallelism) throws ApiException {
        String override = config.getCustomImplementation();
        log.debug("Provided custom implementation: {}", override);

        if (!override.isEmpty()) {
            GenericWebCrawler crawler = implementations
                    .stream()
                    .filter(impl -> impl.getClass().getName().equals(override))
                    .findFirst()
                    .orElseThrow(() -> new ApiException("Implementation not found: " + override));
            System.out.println("Using custom implementation: " + crawler.getClass().getName());
            return crawler;
        }
        GenericWebCrawler crawler = implementations
                .stream()
                .filter(impl -> parallelism <= impl.getMaxConcurrencyLevel())
                .findFirst()
                .orElseThrow(
                        () -> new ApiException(
                                "No implementation able to handle parallelism = \"" +
                                        config.getConcurrencyLevel() + "\"."));
        log.info("Using custom implementation: {}", crawler.getClass().getName());
        return crawler;
    }

    @Provides
    @Singleton
    @ConcurrencyLevel
    int provideTargetParallelism() {
        if (config.getConcurrencyLevel() >= 0) {
            return config.getConcurrencyLevel();
        }
        return Runtime.getRuntime().availableProcessors();
    }

}
