package com.webtracer.crawler.wordcount;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code WordCountUtil} is a utility class that provides functionality for processing word count
 * data
 * collected during a web crawl. This class includes methods for sorting a map of word counts to
 * determine
 * the most frequently occurring words, applying custom sorting criteria such as word frequency,
 * word length,
 * and alphabetical order.
 *
 * <p>The sorting method returns a map containing the top words as specified by the {@code
 * popularWordCount}
 * parameter, which determines how many of the most frequent words should be included in the result.
 * The sorted map is ordered according to the following criteria:
 *
 * <ol>
 *   <li>Word count in descending order (most frequent words first).</li>
 *   <li>Word length in descending order (longer words first).</li>
 *   <li>Alphabetical order to break ties.</li>
 * </ol>

 */
@UtilityClass
final class WordCountUtil {

    /**
     * Sorts an unsorted map of word counts and returns a new map containing the top
     * {@code popularWordCount} words, sorted first by word count in descending order,
     * then by word length in descending order, and finally alphabetically to break ties.
     *
     * @param wordCounts       the unsorted map of word counts.
     * @param popularWordCount the number of top words to include in the result map.
     * @return a map containing the top {@code popularWordCount} words, sorted in the desired order.
     */
    static Map<String, Integer> sort(@NonNull Map<String, Integer> wordCounts, int popularWordCount) {
        if(wordCounts.isEmpty()) return Collections.emptyMap();

        return wordCounts.entrySet().stream()
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
    }

}
