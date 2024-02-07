package com.webtracer.di.annotation;

import com.webtracer.parser.AbstractPageParserFactory;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Qualifier annotation to indicate that the injected {@link AbstractPageParserFactory}
 * is specifically for word count operations in the web crawler.
 * <p>
 * This annotation is used to differentiate between different implementations of
 * {@link AbstractPageParserFactory}. It ensures that the correct factory implementation is injected
 * where word count parsing is required.
 * </p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface WordCountFactory {
}
