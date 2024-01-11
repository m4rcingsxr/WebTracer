package com.webtracer.main.parser.wordcount;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Represents the result of a web crawler parsing operation. This class stores the most
 * frequently occurring words and the list of hyperlinks found on the parsed HTML page.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class WordCountParseResult {

    private final Map<String, Integer> wordFrequencyMap;
    private final List<String> hyperLinkList;

    /**
     * Builder class for constructing {@link WordCountParseResult} instances. This builder
     * tracks word counts and hyperlinks found during the parsing of an HTML page.
     */
    static final class Builder {
        private final Map<String, Integer> wordFrequencyMap = new HashMap<>();
        private final Set<String> hyperlinkList = new HashSet<>();

        /**
         * Increments the count for the specified word. If the word is not already present in
         * the map, it is added with a count of 1. If the word is present, its count is
         * incremented by 1.
         *
         * @param word the word to be added or whose count is to be incremented
         * @throws NullPointerException if the word is null
         */
        void addWord(@NonNull String word) {
            wordFrequencyMap.compute(word, (k, v) -> (v == null) ? 1 : v + 1);
        }

        /**
         * Adds the specified link to the set of hyperlinks. If the link is already present,
         * it is not added again.
         *
         * @param link the hyperlink to be added
         * @throws NullPointerException if the link is null
         */
        void addLink(@NonNull String link) {
            hyperlinkList.add(link);
        }

        /**
         * Constructs a {@link WordCountParseResult} from the current state of the builder.
         * The word frequency map is made unmodifiable, and the hyperlinks are converted to
         * an unmodifiable list.
         *
         * @return a new {@link WordCountParseResult} instance with the current state of the
         *         builder
         */
        WordCountParseResult build() {
            return new WordCountParseResult(
                    Collections.unmodifiableMap(wordFrequencyMap),
                    hyperlinkList.stream().toList()
            );
        }
    }
}