package com.webtracer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuration class for the web crawler.
 * <p>
 * This class holds all the configurable parameters for the web crawler, allowing
 * the crawler's behavior to be customized at runtime.
 * </p>
 */
@Getter
@RequiredArgsConstructor
@Builder
@Jacksonized
public final class WebCrawlerConfig {

    /**
     * A list of initial URLs from which the web crawler will start.
     * <p>
     * This list contains the starting points for the crawling process. If no URLs
     * are provided, the crawler will not start. This field is initialized with an
     * empty list by default.
     */
    @JsonProperty("initialPages")
    @Builder.Default
    private final List<String> initialPages = new ArrayList<>();

    /**
     * A list of regular expressions defining URLs that should be excluded from crawling.
     * <p>
     * URLs matching any of these patterns will not be crawled. This feature is useful
     * for preventing the crawler from accessing irrelevant or repetitive content.
     * Initialized with an empty list by default.
     */
    @JsonProperty("excludedUrls")
    @Builder.Default
    private final List<Pattern> excludedUrls = new ArrayList<>();

    /**
     * A list of regular expressions defining words that should be excluded from the popular word count.
     * <p>
     * Words matching these patterns will be ignored when counting word frequencies
     * during the crawl. This is useful for filtering out common but unimportant words
     * (e.g., stop words). Initialized with an empty list by default.
     */
    @JsonProperty("excludedWords")
    @Builder.Default
    private final List<Pattern> excludedWords = new ArrayList<>();

    /**
     * The fully qualified class name of a custom web crawler implementation.
     * <p>
     * This allows users to override the default crawler implementation with a custom
     * one, specified by this class name. If left empty, the default implementation
     * based on the concurrency level will be used.
     */
    @JsonProperty("customImplementation")
    @Builder.Default
    private final String customImplementation = "";

    /**
     * The maximum depth of links the crawler is allowed to follow.
     * <p>
     * Controls how deep the crawler will go when following links from the initial
     * pages. A depth of 1 restricts the crawler to only the initial pages, while
     * higher values allow for deeper exploration.
     */
    @JsonProperty("maxDepth")
    @Builder.Default
    private final int maxDepth = 1;

    /**
     * The maximum duration allowed for the crawler to run, in seconds.
     * <p>
     * Once this time limit is reached, the crawler will stop fetching new pages
     * but will finish processing any already downloaded HTML. This helps in
     * controlling resource usage and preventing excessively long crawl operations.
     */
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonProperty("timeoutSeconds")
    @Builder.Default
    private final Duration timeout = Duration.ofSeconds(1);

    /**
     * The number of top words to record in the output.
     * <p>
     * Specifies how many of the most frequently occurring words should be recorded
     * in the final result. Useful for generating summaries or analyzing content trends.
     */
    @JsonProperty("popularWordCount")
    @Builder.Default
    private final int popularWordCount = 0;

    /**
     * The file path where the crawl results should be saved.
     * <p>
     * If a valid file path is provided, the crawler's results will be saved there
     * in JSON format. If the path is empty, results will be output to the console.
     */
    @JsonProperty("crawlResultPath")
    @Builder.Default
    private final String crawlResultPath = "";
}
