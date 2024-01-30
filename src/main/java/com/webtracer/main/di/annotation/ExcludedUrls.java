package com.webtracer.main.di.annotation;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code ExcludedUrls} is a custom annotation used to mark the injection of
 * a list of URL patterns that the web crawler should exclude during its operation.
 * <p>
 * This annotation helps ensure that the specified URLs are properly excluded
 * by the crawler when it encounters them, avoiding unnecessary processing of
 * certain links based on the configured patterns.
 * <p>
 * The annotation is retained at runtime to allow the dependency injection
 * framework to use it during the injection process.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludedUrls {
}
