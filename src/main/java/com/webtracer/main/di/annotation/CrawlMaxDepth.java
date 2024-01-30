package com.webtracer.main.di.annotation;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code CrawlMaxDepth} is a custom annotation used to mark the injection of
 * the maximum depth value for the web crawler.
 * <p>
 * This annotation is used with Google Guice to bind and inject the maximum
 * depth that the crawler should follow when visiting links. It ensures that
 * the configured depth is consistently applied across the application.
 * <p>
 * The annotation is retained at runtime to allow the dependency injection
 * framework to use it during the injection process.
 */
@BindingAnnotation
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface CrawlMaxDepth {
}
