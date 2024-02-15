package com.webtracer.di.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.webtracer.di.annotation.ExcludedWords;
import com.webtracer.di.annotation.WordCountFactory;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.DefaultDocumentLoader;
import com.webtracer.parser.wordcount.WordCountPageParserFactoryImpl;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Builder
@Slf4j
public class ParserModule extends AbstractModule {

    private final List<Pattern> excludedWords;
    private final Duration parseTimeout;

    @Override
    protected void configure() {
        log.debug("Configuring ParserModule with excludedWords: {}, parseTimeout: {} ms", excludedWords, parseTimeout.toMillis());

        // Bind the abstract factory to the concrete implementation
        bind(AbstractPageParserFactory.class)
                .annotatedWith(WordCountFactory.class)
                .to(WordCountPageParserFactoryImpl.class);

        log.info("ParserModule configured with WordCountPageParserFactoryImpl");
    }

    @Provides
    @Singleton
    DefaultDocumentLoader provideDefaultDocumentLoader() {
        return new DefaultDocumentLoader(parseTimeout);
    }

    @Provides
    @ExcludedWords
    List<Pattern> provideExcludedWords() {
        return excludedWords;
    }
}
