package com.webtracer.main.crawler.wordcount;

import com.webtracer.main.ApiException;
import com.webtracer.main.parser.wordcount.WordCountPageParserFactoryImpl;
import com.webtracer.main.parser.wordcount.WordCountParseResult;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The {@code SequentialWebCrawler} class is responsible for crawling web pages in a sequential manner,
 * starting from a list of URLs and following links found on those pages up to a specified depth.
 * It counts the frequency of words on the pages it visits, excluding certain URLs based on predefined patterns.
 * The results of the crawl include the most popular words and the total number of URLs visited.
 */
@RequiredArgsConstructor
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
    private final WordCountPageParserFactoryImpl parserFactory;

    /**
     * Starts the crawling process from the given list of starting URLs.
     * It visits each URL, follows links up to the specified depth, and counts word frequencies.
     * The process stops when the timeout is reached or all URLs are processed.
     *
     * @param startingUrls the list of initial URLs to start crawling from.
     * @return a {@link WordCountResult} object containing the most popular words and the total URLs visited.
     */
    @Override
    public WordCountResult crawl(List<String> startingUrls) {

        // Determine the deadline by adding the crawl timeout to the current time.
        Instant deadline = clock.instant().plus(crawlTimeout);

        // Map to hold word counts across all visited pages.
        Map<String, Integer> counts = new HashMap<>();

        // Set to keep track of URLs that have been visited to avoid reprocessing them.
        Set<String> visitedUrls = new HashSet<>();

        // Iterate over each starting URL and begin the crawling process.
        for (String url : startingUrls) {
            crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
        }

        // If no words were counted, return a result with an empty word frequency map.
        if (counts.isEmpty()) {
            return WordCountResult.builder()
                    .wordFrequencyMap(counts)
                    .totalUrlsVisited(visitedUrls.size())
                    .build();
        }

        // Return the results with the sorted word frequencies and the total number of URLs visited.
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
            Set<String> visitedUrls) {

        // Stop crawling if the maximum depth is reached or the timeout has occurred.
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }

        // Skip URLs that match any of the excluded patterns.
        for (Pattern pattern : excludedUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }

        // Skip URLs that have already been visited.
        if (visitedUrls.contains(url)) {
            return;
        }

        WordCountParseResult result = null;
        try {

            // Attempt to parse the page and retrieve the word count results.
            result = parserFactory.createParserInstance(url).parse();
        } catch (ApiException e) {

            // Handle exceptions related to URL parsing, such as malformed or non-existing URLs, or request timeouts.
            // The exception is caught, and the method returns without processing this URL further.
            return;
        }

        // Add the current URL to the set of visited URLs.
        visitedUrls.add(url);

        // Accumulate the word counts from the parsed result.
        for (Map.Entry<String, Integer> e : result.getWordFrequencyMap().entrySet()) {
            counts.merge(e.getKey(), e.getValue(), Integer::sum);
        }

        // Recursively crawl the links found on the current page, decreasing the depth.
        for (String link : result.getHyperLinkList()) {
            crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
        }
    }
}