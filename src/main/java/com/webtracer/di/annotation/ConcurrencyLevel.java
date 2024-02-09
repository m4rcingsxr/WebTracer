package com.webtracer.di.annotation;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for binding the number of CPU cores specified for the crawler's parallelism.
 *
 * <p>The value associated with this annotation is derived from the {@code "parallelism"} setting
 * in the crawler configuration JSON file.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ConcurrencyLevel {
}
