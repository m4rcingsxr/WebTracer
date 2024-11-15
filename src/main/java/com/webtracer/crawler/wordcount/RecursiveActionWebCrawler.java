package com.webtracer.crawler.wordcount;

import com.webtracer.ApiException;
import com.webtracer.RobotsTxtCache;
import com.webtracer.crawler.DomainThrottler;
import com.webtracer.di.annotation.*;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.wordcount.WordCountPageParserImpl;
import com.webtracer.parser.wordcount.WordCountParseResult;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

/**
 * A web crawler that uses the Fork/Join framework to recursively process web pages in parallel.
 * This crawler is designed to extract word counts from web pages and supports configurable
 * concurrency, depth limits, and URL exclusion patterns. It also includes a domain throttling
 * mechanism to avoid overwhelming servers.
 *
 * <p>The Fork/Join framework allows this crawler to efficiently handle recursive tasks in parallel,
 * with each web page being processed as a separate task that can spawn subtasks for hyperlinks.</p>
 */
@Slf4j
public class RecursiveActionWebCrawler implements WordCountWebCrawler {

    private final Clock systemClock;
    private final AbstractPageParserFactory parserFactory;
    private final Duration crawlTimeout;
    private final int topWordCount;
    private final ForkJoinPool threadPool;
    private final List<Pattern> excludedUrls;
    private final int maximumDepth;
    private final DomainThrottler domainThrottler;
    private final RobotsTxtCache robotsTxtCache;

    /**
     * Constructs a RecursiveActionWebCrawler with the specified parameters, including domain
     * throttling.
     *
     * @param systemClock      the clock to use for timing operations
     * @param parserFactory    the factory to create parsers for processing web pages
     * @param crawlTimeout     the maximum duration to allow for crawling
     * @param topWordCount     the maximum number of words to include in the result
     * @param concurrencyLevel the maximum level of concurrency allowed; this controls the number of threads
     *                         that can be used simultaneously by the ForkJoinPool.
     * @param maximumDepth     the maximum depth to crawl
     * @param excludedUrls     a list of URL patterns to exclude from crawling
     * @param domainThrottler  the throttler to control request rates per domain
     */
    @Inject
    public RecursiveActionWebCrawler(
            Clock systemClock,
            @WordCountFactory AbstractPageParserFactory parserFactory,
            @CrawlTimeout Duration crawlTimeout,
            @PopularWordCount int topWordCount,
            @ConcurrencyLevel int concurrencyLevel,
            @CrawlMaxDepth int maximumDepth,
            @ExcludedUrls List<Pattern> excludedUrls,
            DomainThrottler domainThrottler
    ) {
        this.systemClock = systemClock;
        this.parserFactory = parserFactory;
        this.crawlTimeout = crawlTimeout;
        this.topWordCount = topWordCount;
        this.threadPool = new ForkJoinPool(Math.min(concurrencyLevel, getMaxConcurrencyLevel()));
        this.maximumDepth = maximumDepth;
        this.excludedUrls = excludedUrls;
        this.domainThrottler = domainThrottler;
        this.robotsTxtCache = new RobotsTxtCache("WebTracer");
        log.info(
                "Initialized RecursiveActionWebCrawler with max depth: {}, concurrency level: {}," +
                        " crawl timeout: {}, and domain throttling.",
                maximumDepth, concurrencyLevel, crawlTimeout
        );
    }

    /**
     * Crawls the web starting from the given initial pages.
     *
     * @param initialPages a list of URLs to start crawling from
     * @return a WordCountResult containing the word frequencies and the total number of visited
     * URLs
     * @throws ApiException if an error occurs during crawling
     *
     * <p>This method is thread-safe due to the use of {@link ConcurrentHashMap} for word counts and
     * {@link ConcurrentSkipListSet} for tracking visited URLs. Each URL is processed in a separate
     * task, and results are merged safely across threads.</p>
     */
    @Override
    public WordCountResult crawl(List<String> initialPages) throws ApiException {
        log.info("Starting crawl with initial pages: {}", initialPages);

        Instant deadline = systemClock.instant().plus(crawlTimeout);
        Map<String, Integer> wordCounts = new ConcurrentHashMap<>();
        Set<String> visitedUrls = new ConcurrentSkipListSet<>();

        for (String url : initialPages) {
            log.debug("Invoking crawl action for URL: {}", url);
            threadPool.invoke(
                    new RecursiveActionImpl(systemClock, crawlTimeout, deadline, url, wordCounts,
                                            visitedUrls, parserFactory, maximumDepth, excludedUrls,
                                            domainThrottler,robotsTxtCache
                    ));
        }

        log.info("Crawl completed. Total URLs visited: {}", visitedUrls.size());

        if (wordCounts.isEmpty()) {
            log.warn("No words found during the crawl.");
            return WordCountResult.builder()
                    .wordFrequencyMap(wordCounts)
                    .totalUrlsVisited(visitedUrls.size())
                    .build();
        }

        return WordCountResult.builder()
                .wordFrequencyMap(WordCountUtil.sort(wordCounts, topWordCount))
                .totalUrlsVisited(visitedUrls.size())
                .build();
    }

    /**
     * A RecursiveAction implementation for web crawling that processes a given URL
     * and recursively invokes itself for each hyperlink found on the page.
     *
     * <p>This class leverages the Fork/Join framework to split the crawling process into smaller tasks,
     * each handling a single URL and its subsequent hyperlinks. Subtasks are handled concurrently using
     * the `invokeAll` method, which efficiently manages the parallel execution of these subtasks.</p>
     */
    @RequiredArgsConstructor
    static final class RecursiveActionImpl extends RecursiveAction {

        private final Clock systemClock;
        private final Duration crawlTimeout;
        private final Instant crawlDeadline;
        private final String currentUrl;
        private final Map<String, Integer> wordCounts;
        private final Set<String> visitedUrls;
        private final AbstractPageParserFactory parserFactory;
        private final int remainingDepth;
        private final List<Pattern> excludedUrlPatterns;
        private final DomainThrottler domainThrottler;
        private final RobotsTxtCache robotsTxtCache;

        /**
         * Processes the current URL by parsing its content, updating word counts, and recursively
         * invoking subtasks for each hyperlink found on the page.
         *
         * <p>This method is part of a Fork/Join framework implementation, allowing the crawler to
         * efficiently handle large-scale web crawling tasks in parallel.
         * </p>
         */
        @Override
        protected void compute() {
            log.debug("Processing URL: {}", currentUrl);

            // Check if the maximum depth has been reached or if the deadline has passed.
            if (remainingDepth == 0 || systemClock.instant().isAfter(crawlDeadline)) {
                log.debug("Stopping crawl at URL: {} due to depth limit or timeout", currentUrl);
                return;
            }

            if (!robotsTxtCache.isAllowed(URI.create(currentUrl))) {
                return;
            }

            // Check if the URL matches any of the ignored URL patterns.
            for (Pattern pattern : excludedUrlPatterns) {
                if (pattern.matcher(currentUrl).matches()) {
                    log.debug("Excluding URL: {} based on exclusion pattern", currentUrl);
                    return;
                }
            }

            // Check if the URL has already been visited.
            if (!visitedUrls.add(currentUrl)) {
                log.debug("Skipping already visited URL: {}", currentUrl);
                return;
            }

            // Throttle the request based on the domain
            try {
                String domain = extractDomain(currentUrl);
                log.debug("Throttling domain: {} before processing URL: {}", domain, currentUrl);
                domainThrottler.acquire(domain);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrupted while throttling domain for URL: {}", currentUrl);
                return;
            }

            // Parse the current URL.
            log.debug("Parsing URL: {}", currentUrl);
            WordCountParseResult result =
                    ((WordCountPageParserImpl) parserFactory.createParserInstance(currentUrl)).parse();

            // Update word counts with the parsed data.
            result.getWordFrequencyMap().forEach((key, value) -> wordCounts.merge(key, value, Integer::sum));

            // Create and invoke subtasks for each hyperlink found on the page.
            List<RecursiveActionImpl> subtasks = result.getHyperLinkList().stream()
                    .map(link -> new RecursiveActionImpl(systemClock, crawlTimeout, crawlDeadline,
                                                         link, wordCounts, visitedUrls,
                                                         parserFactory, remainingDepth - 1,
                                                         excludedUrlPatterns, domainThrottler, robotsTxtCache
                    ))
                    .toList();

            log.debug("Invoking subtasks for URL: {} with {} hyperlinks", currentUrl,
                      subtasks.size()
            );

            // Invoke all subtasks concurrently.
            invokeAll(subtasks);
        }

        /**
         * Extracts the domain from a given URL.
         *
         * @param url the URL to extract the domain from
         * @return the domain of the URL, or the URL itself if the domain cannot be extracted
         */
        private String extractDomain(String url) {
            try {
                URI uri = new URI(url);
                String domain = uri.getHost();
                log.debug("Extracted domain: {} from URL: {}", domain, url);
                return domain;
            } catch (URISyntaxException e) {
                log.error("Failed to extract domain from URL: {}", url, e);
                return url; // Return the full URL if domain extraction fails
            }
        }
    }
}
