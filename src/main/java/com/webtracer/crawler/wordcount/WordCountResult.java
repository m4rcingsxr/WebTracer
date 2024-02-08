package com.webtracer.crawler.wordcount;

import com.webtracer.crawler.CrawlResult;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * The {@code WordCountResult} class represents the result of a web crawling operation specifically
 * focused on counting words across the crawled web pages. It extends the {@link CrawlResult} class
 * to provide a concrete representation of the crawl outcome with word count data.
 *
 * <p>This class is designed to be immutable, ensuring that the results of the crawl cannot be modified
 * after they have been created. It contains information such as the word frequency distribution and
 * any other relevant data extracted during the crawl.
 *
 * <p>Instances of this class can be returned by the `crawl` method in the
 * {@link WordCountWebCrawler} interface, providing a structured
 * way to access the results of a word count-focused web crawl.
 */
@Getter
@Slf4j
public final class WordCountResult extends CrawlResult {

    /**
     * An unmodifiable {@link Map} where each key is a word encountered during the web crawl, and each value
     * is the number of times the word was seen across all crawled pages.
     *
     * <p>Word counts are unique per page and reflect the total occurrences across distinct pages.
     *
     * <p>The size of the map corresponds to the {@code "popularWordCount"} setting in the crawler configuration.
     * For example, if {@code "popularWordCount"} is 3, only the top 3 most frequent words are included.
     *
     * <p>If multiple words share the same frequency, longer words rank higher. If both frequency and length
     * are identical, alphabetical order is used to rank them.
     */
    @NonNull
    private final Map<String, Integer> wordFrequencyMap;

    @Builder
    public WordCountResult(int totalUrlsVisited, @NonNull Map<String, Integer> wordFrequencyMap) {
        super(totalUrlsVisited);
        this.wordFrequencyMap = wordFrequencyMap;

        log.debug("WordCountResult created with totalUrlsVisited = {} and wordFrequencyMap = {}",
                  totalUrlsVisited, wordFrequencyMap);
    }
}
