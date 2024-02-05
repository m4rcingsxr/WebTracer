package com.webtracer.di.module;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.GenericWebCrawler;
import com.webtracer.crawler.wordcount.SequentialWebCrawler;
import com.webtracer.di.annotation.*;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.wordcount.WordCountPageParserFactoryImpl;
import jakarta.inject.Qualifier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
                Multibinder.newSetBinder(binder(), GenericWebCrawler.class, Internal.class);
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
    @Internal
    GenericWebCrawler provideRawWebCrawler(
            @Internal Set<GenericWebCrawler> implementations) {

        String override = config.getCustomImplementation();

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

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface Internal {
    }
}
