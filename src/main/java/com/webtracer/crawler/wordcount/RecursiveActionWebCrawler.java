package com.webtracer.crawler.wordcount;

import com.webtracer.ApiException;
import com.webtracer.di.annotation.*;
import com.webtracer.parser.AbstractPageParserFactory;
import com.webtracer.parser.ParseResult;
import com.webtracer.parser.wordcount.WordCountPageParserImpl;
import com.webtracer.parser.wordcount.WordCountParseResult;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

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

public class RecursiveActionWebCrawler implements WordCountWebCrawler {

    private final Clock systemClock;
    private final AbstractPageParserFactory parserFactory;
    private final Duration crawlTimeout;
    private final int topWordCount;
    private final ForkJoinPool threadPool;
    private final List<Pattern> excludedUrls;
    private final int maximumDepth;

    @Inject
    RecursiveActionWebCrawler(
            Clock systemClock,
            @WordCountFactory AbstractPageParserFactory parserFactory,
            @CrawlTimeout Duration crawlTimeout,
            @PopularWordCount int topWordCount,
            @ConcurrencyLevel int concurrencyLevel,
            @CrawlMaxDepth int maximumDepth,
            @ExcludedUrls List<Pattern> excludedUrls
    ) {
        this.systemClock = systemClock;
        this.parserFactory = parserFactory;
        this.crawlTimeout = crawlTimeout;
        this.topWordCount = topWordCount;
        this.threadPool = new ForkJoinPool(Math.min(concurrencyLevel, getMaxConcurrencyLevel()));
        this.maximumDepth = maximumDepth;
        this.excludedUrls = excludedUrls;
    }

    @Override
    public WordCountResult crawl(List<String> initialPages) throws ApiException {
        Instant deadline = systemClock.instant().plus(crawlTimeout);
        Map<String, Integer> wordCounts = new ConcurrentHashMap<>();
        Set<String> visitedUrls = new ConcurrentSkipListSet<>();
        for (String url : initialPages) {
            threadPool.invoke(new RecursiveActionImpl(systemClock, crawlTimeout, deadline, url, wordCounts, visitedUrls, parserFactory, maximumDepth, excludedUrls));
        }

        if (wordCounts.isEmpty()) {
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
     */
    @RequiredArgsConstructor
    static final class RecursiveActionImpl extends RecursiveAction {

        // Clock instance to manage time-related operations.
        private final Clock systemClock;

        // Maximum allowed duration for the crawl operation.
        private final Duration crawlTimeout;

        // Deadline for the crawl operation.
        private final Instant crawlDeadline;

        // URL to be processed.
        private final String currentUrl;

        // Map to store word counts.
        private final Map<String, Integer> wordCounts;

        // Set to track visited URLs to avoid revisits.
        private final Set<String> visitedUrls;

        // Factory to create PageParser instances.
        private final AbstractPageParserFactory parserFactory;

        // Maximum depth for crawling.
        private final int remainingDepth;

        // List of URL patterns to be ignored.
        private final List<Pattern> excludedUrlPatterns;

        /**
         * The main computation performed by this action.
         */
        @Override
        protected void compute() {
            // Check if the maximum depth has been reached or if the deadline has passed.
            if (remainingDepth == 0 || systemClock.instant().isAfter(crawlDeadline)) {
                return;
            }
            // Check if the URL matches any of the ignored URL patterns.
            for (Pattern pattern : excludedUrlPatterns) {
                if (pattern.matcher(currentUrl).matches()) {
                    return;
                }
            }
            // Check if the URL has already been visited.
            if (visitedUrls.contains(currentUrl)) {
                return;
            }
            // Mark the URL as visited.
            visitedUrls.add(currentUrl);
            // Parse the current URL.
            WordCountParseResult result = ((WordCountPageParserImpl)parserFactory.createParserInstance(currentUrl)).parse();
            // Update word counts with the parsed data.
            for (Map.Entry<String, Integer> entry : result.getWordFrequencyMap().entrySet()) {
                wordCounts.compute(entry.getKey(), (key, value) -> {
                    if (value == null) {
                        return entry.getValue();
                    } else {
                        return value + entry.getValue();
                    }
                });
            }
            // Create subtasks for each hyperlink found on the page.
            List<RecursiveActionImpl> subtasks = result.getHyperLinkList().stream()
                    .map(link -> new RecursiveActionImpl(systemClock, crawlTimeout, crawlDeadline, link, wordCounts, visitedUrls, parserFactory, remainingDepth - 1, excludedUrlPatterns))
                    .toList();
            // Invoke all subtasks.
            invokeAll(subtasks);
        }
    }

}
