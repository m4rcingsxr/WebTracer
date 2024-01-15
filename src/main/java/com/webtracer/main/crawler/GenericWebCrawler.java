package com.webtracer.main.crawler;


import java.util.List;

/**
 * This interface defines a generic web crawler that can be implemented to perform web crawling tasks.
 * The crawler is initialized with a list of starting URLs and processes the web pages to extract the
 * desired information. The result of the crawl is encapsulated in a {@link CrawlResult} object.
 */
public interface GenericWebCrawler {

    /**
     * Starts the web crawling process using the provided list of initial URLs. The implementation
     * of this method should define how the crawling is performed, including handling of HTTP requests,
     * parsing of web pages, following links, and managing the crawl depth.
     *
     * @param initialUrls a list of URLs to start the crawling process from. These URLs serve as the
     *                    entry points for the crawler.
     * @return a {@link CrawlResult} object containing the outcome of the crawl, including any data
     *         extracted from the web pages and any errors encountered during the process.
     */
    CrawlResult crawl(List<String> initialUrls);

    /**
     * Returns the maximum concurrency level supported by this web crawler, which is typically
     * determined by the number of available CPU cores. This method provides a default implementation
     * that returns the number of processors available to the Java Virtual Machine.
     *
     * The maximum concurrency level can be used by implementations to optimize the crawling process
     * by running multiple threads in parallel, thereby improving the efficiency and speed of the
     * crawling task.
     *
     * @return the maximum number of parallel threads that can be used by the crawler, based on the
     *         number of available CPU cores.
     */
    default int getMaxConcurrencyLevel() {
        return Runtime.getRuntime().availableProcessors();
    }
}