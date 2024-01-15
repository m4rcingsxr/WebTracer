package com.webtracer.main.crawler;

/**
 * This interface represents the result of a web crawling operation. It serves as a container for
 * any data extracted during the crawling process, as well as any metadata or status information
 * that is relevant to the outcome of the crawl.
 *
 * Implementations of this interface can provide specific details about the results, such as the
 * list of successfully crawled URLs, any errors encountered, and the extracted content or data.
 *
 * The {@link GenericWebCrawler} interface's `crawl` method returns an instance of this interface,
 * encapsulating the final outcome of the crawling process.
 */
public interface CrawlResult {
}