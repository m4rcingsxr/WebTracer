package com.webtracer.di.annotation;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code ExcludedWords} is a custom annotation used to mark the injection of
 * a list of words that should be excluded from the word count process by the crawler.
 * <p>
 * This annotation is used with Google Guice to bind and inject the specific
 * list of words that are to be ignored during the word counting operation,
 * ensuring consistency across the application.
 * <p>
 * The annotation is retained at runtime to allow the dependency injection
 * framework to use it during the injection process.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludedWords {
}
