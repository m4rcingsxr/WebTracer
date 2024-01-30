package com.webtracer.main.di.annotation;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code PopularWordCount} is a custom annotation used to mark the injection
 * of the number of top most popular words that should be retained in the result
 * by the web crawler.
 * <p>
 * This annotation is utilized in conjunction with Google Guice to bind and
 * inject the specific count value, ensuring that the crawler consistently
 * applies this setting throughout its operations.
 * <p>
 * The annotation is retained at runtime to allow the dependency injection
 * framework to use it during the injection process.
 */
@BindingAnnotation
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface PopularWordCount {
}
