package com.webtracer.di.module;

import com.google.inject.AbstractModule;
import com.webtracer.di.annotation.WordCountFactory;
import com.webtracer.parser.AbstractPageParserFactory;
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
    private final Duration crawlTimeout;

    @Override
    protected void configure() {
        log.debug("Configuring ParserModule with excludedWords: {}, crawlTimeout: {} ms", excludedWords, crawlTimeout.toMillis());

        bind(AbstractPageParserFactory.class)
                .annotatedWith(WordCountFactory.class)
                .toInstance(new WordCountPageParserFactoryImpl(excludedWords, crawlTimeout));



        log.info("ParserModule configured with WordCountPageParserFactoryImpl");
    }
}
