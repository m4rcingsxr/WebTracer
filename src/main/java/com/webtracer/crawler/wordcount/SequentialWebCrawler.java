package com.webtracer.crawler.wordcount;

import com.google.inject.Inject;
import com.webtracer.di.annotation.*;
import com.webtracer.ApiException;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.ParseResult;
import com.webtracer.parser.wordcount.WordCountParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The {@code SequentialWebCrawler} class is a concrete implementation of the {@link WordCountWebCrawler}
 * interface, responsible for crawling web pages in a sequential manner, starting from a list of URLs
 * and following links found on those pages up to a specified depth.
 * <p>
 * This crawler counts the frequency of words on the pages it visits, excluding certain URLs based on
 * predefined patterns. The results of the crawl include the most popular words and the total number
 * of URLs visited.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public final class SequentialWebCrawler implements WordCountWebCrawler {

    // Clock instance to manage time-based operations, useful for testing with fixed or custom clocks.
    private final Clock clock;

    // Duration after which the crawling process should timeout.
    private final Duration crawlTimeout;

    // Maximum depth to which the crawler should follow links from the starting URLs. 1 = only provided url
    private final int maxDepth;

    // The number of the most popular words to include in the results.
    private final int popularWordCount;

    // List of URL patterns to be excluded from crawling.
    private final List<Pattern> excludedUrls;

    // Factory to create page parsers specific to the word count functionality.
    private final AbstractPageParserFactory parserFactory;

    @Inject
    SequentialWebCrawler(
            Clock clock,
            @WordCountFactory AbstractPageParserFactory parserFactory,
            @CrawlTimeout Duration crawlTimeout,
            @PopularWordCount int popularWordCount,
            @CrawlMaxDepth int maxDepth,
            @ExcludedUrls List<Pattern> excludedUrls
    ) {
        this.clock = clock;
        this.crawlTimeout = crawlTimeout;
        this.popularWordCount = popularWordCount;
        this.maxDepth = maxDepth;
        this.excludedUrls = excludedUrls;
        this.parserFactory = parserFactory;
    }

    /**
     * Starts the crawling process from the given list of starting URLs.
     * It visits each URL, follows links up to the specified depth, and counts word frequencies.
     * The process stops when the timeout is reached or all URLs are processed.
     *
     * @param startingUrls the list of initial URLs to start crawling from.
     * @return a {@link WordCountResult} object containing the most popular words and the total URLs visited.
     * @throws ApiException if an error occurs during the crawling process, such as network issues,
     *                      parsing errors, or unexpected response formats.
     */
    @Override
    public WordCountResult crawl(List<String> startingUrls) throws ApiException {
        log.info("Starting crawl with {} starting URLs", startingUrls.size());

        Instant deadline = clock.instant().plus(crawlTimeout);
        Map<String, Integer> counts = new HashMap<>();
        Set<String> visitedUrls = new HashSet<>();

        for (String url : startingUrls) {
            log.debug("Crawling URL: {}", url);
            crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
        }

        if (counts.isEmpty()) {
            log.warn("No words counted during the crawl");
            return WordCountResult.builder()
                    .wordFrequencyMap(counts)
                    .totalUrlsVisited(visitedUrls.size())
                    .build();
        }

        log.info("Crawl completed with {} URLs visited", visitedUrls.size());
        return WordCountResult.builder()
                .wordFrequencyMap(WordCountUtil.sort(counts, popularWordCount))
                .totalUrlsVisited(visitedUrls.size())
                .build();
    }

    /**
     * Internal method to handle the crawling of a single URL. This method is called recursively to
     * follow links to the specified depth and accumulate word counts.
     *
     * @param url          the URL to be crawled.
     * @param deadline     the time at which the crawling should stop.
     * @param maxDepth     the maximum depth to which the crawler should follow links.
     * @param counts       a map to accumulate word counts across all visited pages.
     * @param visitedUrls  a set to keep track of visited URLs to prevent reprocessing.
     */
    private void crawlInternal(
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) throws ApiException {

        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            log.trace("Stopping crawl at URL: {} due to depth or timeout", url);
            return;
        }

        for (Pattern pattern : excludedUrls) {
            if (pattern.matcher(url).matches()) {
                log.debug("Skipping URL: {} due to exclusion pattern", url);
                return;
            }
        }

        if (visitedUrls.contains(url)) {
            log.debug("Skipping already visited URL: {}", url);
            return;
        }

        try {
            ParseResult result = parserFactory.createParserInstance(url).parse();
            visitedUrls.add(url);

            for (Map.Entry<String, Integer> entry : ((WordCountParseResult) result).getWordFrequencyMap().entrySet()) {
                counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }

            for (String link : ((WordCountParseResult) result).getHyperLinkList()) {
                crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
            }
        } catch (ApiException e) {
            log.error("Error parsing URL: {}", url, e);
        }
    }

    @Override
    public int getMaxConcurrencyLevel() {
        return 1;
    }

}