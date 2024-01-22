package com.webtracer.main.crawler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The {@code CrawlResult} abstract class represents the result of a web crawling operation. It serves as a base class
 * for storing any data extracted during the crawling process, as well as any relevant metadata or status information.
 *
 * <p>This class can be extended to provide specific details about the crawl results, such as the list of successfully
 * crawled URLs, any errors encountered, or any extracted content. It provides a common structure for all crawl results
 * while allowing subclasses to add their specialized data.
 *
 * <p>The {@link GenericWebCrawler} interface's `crawl` method returns an instance of this class or one of its subclasses,
 * encapsulating the final outcome of the crawling process.
 */
@Getter
@RequiredArgsConstructor
public abstract class CrawlResult {

    /**
     * The total number of distinct URLs that the web crawler visited during the operation.
     *
     * <p>A URL is considered "visited" if an attempt was made to crawl it, even if the HTTP request resulted in an error.
     * Each URL is counted only once, regardless of multiple visits or retries.
     */
    private final int totalUrlsVisited;
}