package com.webtracer.di.module;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.webtracer.di.annotation.WordCountFactory;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.wordcount.WordCountPageParserFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParserModuleTest {

    private Injector injector;

    @BeforeEach
    void setUp() {
        List<Pattern> excludedWords = List.of(Pattern.compile(".*test.*"));
        Duration crawlTimeout = Duration.ofSeconds(5);

        ParserModule parserModule = ParserModule.builder()
                .excludedWords(excludedWords)
                .parseTimeout(crawlTimeout)
                .build();

        injector = Guice.createInjector(parserModule);
    }

    @Test
    void givenParserModule_whenInjected_thenShouldProvideWordCountPageParserFactoryImpl() {
        AbstractPageParserFactory factory = injector.getInstance(Key.get(AbstractPageParserFactory.class, WordCountFactory.class));

        assertNotNull(factory);
        assertInstanceOf(WordCountPageParserFactoryImpl.class, factory);
    }

}
