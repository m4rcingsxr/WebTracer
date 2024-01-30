package com.webtracer.main.di.annotation;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code CrawlTimeout} is a custom annotation used to mark the injection of
 * a timeout duration for the web crawler.
 * <p>
 * This annotation is used in conjunction with Google Guice to specify the
 * particular binding for the crawler's timeout duration, ensuring that the
 * correct value is injected wherever required.
 * <p>
 * The annotation is retained at runtime to allow the dependency injection
 * framework to use it during the injection process.
 */
@BindingAnnotation
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface CrawlTimeout {
}
