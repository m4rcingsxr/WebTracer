package com.webtracer.parser.wordcount;

import com.webtracer.parser.ParseResult;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * The {@code WordCountParseResult} class represents the outcome of a web page parsing operation
 * focused on word counting. It stores the frequency of words found on the page and the list
 * of hyperlinks extracted from the parsed HTML content.
 *
 * <p>This class is immutable and should be instantiated using the nested {@link Builder} class.
 * The {@code Builder} class allows for the incremental construction of a {@code WordCountParseResult}
 * by tracking word counts and hyperlinks as they are found during parsing.</p>
 */
@Getter
@Slf4j
@RequiredArgsConstructor
public final class WordCountParseResult implements ParseResult {

    /**
     * A map containing the word frequencies from the parsed web page.
     * The keys are words found on the page, and the values are the respective counts of those words.
     */
    @NonNull
    private final Map<String, Integer> wordFrequencyMap;

    /**
     * A list of hyperlinks found on the parsed web page.
     * Each entry in the list represents a distinct hyperlink present in the HTML content.
     */
    @NonNull
    private final List<String> hyperLinkList;

    /**
     * The {@code Builder} class facilitates the construction of {@link WordCountParseResult} instances.
     * It accumulates word counts and hyperlinks during the parsing of an HTML document, allowing for
     * incremental updates to the result state before finalizing the build.
     */
    @Slf4j
    public static final class Builder {
        private final Map<String, Integer> wordFrequencyMap = new HashMap<>();
        private final Set<String> hyperlinkList = new HashSet<>();

        /**
         * Adds a word to the word frequency map or increments its count if it already exists.
         * If the word is not already in the map, it is added with a count of 1.
         *
         * @param word the word to be added or whose count should be incremented.
         * @throws NullPointerException if the word is {@code null}.
         * @return this {@code Builder} instance, allowing for method chaining.
         */
        public Builder addWord(@NonNull String word) {
            wordFrequencyMap.compute(word, (k, v) -> (v == null) ? 1 : v + 1);
            log.trace("Added/incremented word: {} (current count: {})", word, wordFrequencyMap.get(word));
            return this;
        }

        /**
         * Adds a hyperlink to the set of hyperlinks found during parsing.
         * Duplicate links are not added again.
         *
         * @param link the hyperlink to be added.
         * @throws NullPointerException if the link is {@code null}.
         * @return this {@code Builder} instance, allowing for method chaining.
         */
        public Builder addLink(@NonNull String link) {
            if (hyperlinkList.add(link)) {
                log.trace("Added hyperlink: {}", link);
            } else {
                log.trace("Hyperlink already exists, not adding: {}", link);
            }
            return this;
        }

        /**
         * Builds a {@link WordCountParseResult} instance from the current state of the builder.
         * The word frequency map is made unmodifiable, and the hyperlinks are stored in an unmodifiable list.
         *
         * @return a new {@link WordCountParseResult} instance reflecting the accumulated data.
         */
        public WordCountParseResult build() {
            log.debug("Building WordCountParseResult with {} words and {} hyperlinks",
                      wordFrequencyMap.size(), hyperlinkList.size());
            return new WordCountParseResult(
                    Collections.unmodifiableMap(wordFrequencyMap),
                    hyperlinkList.stream().toList()
            );
        }
    }
}
