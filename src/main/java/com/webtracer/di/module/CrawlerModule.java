package com.webtracer.di.module;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.GenericWebCrawler;
import com.webtracer.crawler.wordcount.SequentialWebCrawler;
import com.webtracer.di.annotation.CrawlMaxDepth;
import com.webtracer.di.annotation.CrawlTimeout;
import com.webtracer.di.annotation.ExcludedUrls;
import com.webtracer.di.annotation.PopularWordCount;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CrawlerModule extends AbstractModule {

    @NonNull
    private final WebCrawlerConfig config;

    @Override
    protected void configure() {
        Multibinder<GenericWebCrawler> multibinder =
                Multibinder.newSetBinder(binder(), GenericWebCrawler.class);
        multibinder.addBinding().to(SequentialWebCrawler.class);

        bind(Clock.class).toInstance(Clock.systemUTC());
        bind(Key.get(Integer.class, CrawlMaxDepth.class)).toInstance(config.getMaxDepth());
        bind(Key.get(Integer.class, PopularWordCount.class)).toInstance(
                config.getPopularWordCount());
        bind(Key.get(Duration.class, CrawlTimeout.class)).toInstance(config.getTimeout());
        bind(new Key<List<Pattern>>(ExcludedUrls.class) {
        }).toInstance(config.getExcludedUrls());

        install(
                ParserModule.builder()
                        .excludedWords(config.getExcludedWords())
                        .crawlTimeout(config.getTimeout())
                        .build()
        );
    }

    @Provides
    @Singleton
    GenericWebCrawler provideRawWebCrawler(
            Set<GenericWebCrawler> implementations) {

        String override = config.getCustomImplementation();
        System.out.println(override);

        if (override.isEmpty()) {
            throw new IllegalStateException("Implementation has not been set");
        }

        GenericWebCrawler crawler = implementations
                .stream()
                .filter(impl -> impl.getClass().getName().equals(override))
                .findFirst()
                .orElseThrow(() -> new ProvisionException("Implementation not found: " + override));

        System.out.println("Using custom implementation: " + crawler.getClass().getName());

        return crawler;
    }

}
