package com.webtracer.di.module;

import com.google.inject.AbstractModule;
import com.webtracer.di.annotation.WordCountFactory;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.wordcount.WordCountPageParserFactoryImpl;
import lombok.Builder;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Builder
public class ParserModule extends AbstractModule {

    private final List<Pattern> excludedWords;
    private final Duration crawlTimeout;

    @Override
    protected void configure() {
        bind(AbstractPageParserFactory.class)
                .annotatedWith(WordCountFactory.class)
                .toInstance(new WordCountPageParserFactoryImpl(excludedWords, crawlTimeout));
    }

}
