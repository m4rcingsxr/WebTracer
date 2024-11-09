package com.webtracer.crawler.wordcount;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code WordCountUtil} is a utility class that provides functionality for processing word count
 * data collected during a web crawl. This class includes methods for sorting a map of word counts
 * to determine the most frequently occurring words, applying custom sorting criteria such as word
 * frequency, word length, and alphabetical order.
 *
 * <p>The sorting method returns a map containing the top words as specified by the {@code
 * popularWordCount} parameter, which determines how many of the most frequent words should be
 * included in the result. The sorted map is ordered according to the following criteria:</p>
 *
 * <ol>
 *   <li>Word frequency in descending order (most frequent words first).</li>
 *   <li>Word length in descending order (longer words first, when frequencies are equal).</li>
 *   <li>Alphabetical order to break ties between words with the same frequency and length.</li>
 * </ol>
 *
 * <p>This utility class is designed to be used within the context of web crawlers or text
 * analysis tools that need to identify and rank popular words based on their occurrences in
 * collected data.</p>
 */
@UtilityClass
@Slf4j
final class WordCountUtil {

    /**
     * Sorts an unsorted map of word counts and returns a new map containing the top
     * {@code popularWordCount} words, sorted by the following criteria:
     *
     * <ol>
     *   <li>Word frequency in descending order.</li>
     *   <li>Word length in descending order, for words with the same frequency.</li>
     *   <li>Alphabetical order to break ties between words with the same frequency and length.</li>
     * </ol>
     *
     * <p>This method ensures that the most significant words, according to the defined criteria,
     * are prioritized and returned in a predictable order.</p>
     *
     * @param wordCounts       the unsorted map of word counts.
     * @param popularWordCount the number of top words to include in the result map.
     * @return a map containing the top {@code popularWordCount} words, sorted in the specified order.
     */
    static Map<String, Integer> sort(@NonNull Map<String, Integer> wordCounts, int popularWordCount) {
        log.debug("Starting sort of word counts with popularWordCount = {}", popularWordCount);

        if (wordCounts.isEmpty()) {
            log.info("No word counts to sort; returning empty map");
            return Collections.emptyMap();
        }

        Map<String, Integer> sortedWordCounts = wordCounts.entrySet().parallelStream()
                .sorted(
                        Comparator.comparing(Map.Entry<String, Integer>::getValue)
                                .reversed()
                                .thenComparing(entry -> entry.getKey().length(), Comparator.reverseOrder())
                                .thenComparing(Map.Entry::getKey)
                )
                .limit(popularWordCount)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum,
                        LinkedHashMap::new
                ));

        log.debug("Completed sorting of word counts. Top {} words: {}", popularWordCount, sortedWordCounts);
        return sortedWordCounts;
    }
}
