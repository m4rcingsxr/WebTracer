package com.webtracer.main.crawler.wordcount;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WordCountUtilTest {

    @Test
    void givenUnsortedWordCounts_whenSortIsCalled_thenShouldSortByFrequencyLengthAndAlphabetically() {
        Map<String, Integer> input = Map.of("apple", 2, "banana", 3, "carrot", 1);
        Map<String, Integer> result = WordCountUtil.sort(input, 3);
        Map<String, Integer> expected = Map.of("banana", 3, "apple", 2, "carrot", 1);

        assertEquals(expected, result);
    }

    @Test
    void givenWordCountsWithSameFrequency_whenSortIsCalled_thenShouldSortByLengthAndAlphabetically() {
        Map<String, Integer> input = Map.of("apple", 2, "banana", 2, "cat", 2);
        Map<String, Integer> result = WordCountUtil.sort(input, 3);
        Map<String, Integer> expected = Map.of("banana", 2, "apple", 2, "cat", 2);

        assertEquals(expected, result);
    }

    @Test
    void givenWordCountsWithSameFrequencyAndLength_whenSortIsCalled_thenShouldSortAlphabetically() {
        Map<String, Integer> input = Map.of("apple", 2, "grape", 2, "peach", 2);
        Map<String, Integer> result = WordCountUtil.sort(input, 3);
        Map<String, Integer> expected = Map.of("apple", 2, "grape", 2, "peach", 2);

        assertEquals(expected, result);
    }

    @Test
    void givenMoreWordsThanPopularWordCount_whenSortIsCalled_thenShouldLimitToPopularWordCount() {
        Map<String, Integer> input = Map.of("apple", 3, "banana", 2, "cat", 1);
        Map<String, Integer> result = WordCountUtil.sort(input, 2);
        Map<String, Integer> expected = Map.of("apple", 3, "banana", 2);

        assertEquals(expected, result);
    }

    @Test
    void givenFewerWordsThanPopularWordCount_whenSortIsCalled_thenShouldIncludeAllWords() {
        Map<String, Integer> input = Map.of("apple", 3, "banana", 2);
        Map<String, Integer> result = WordCountUtil.sort(input, 5);
        Map<String, Integer> expected = Map.of("apple", 3, "banana", 2);

        assertEquals(expected, result);
    }

    @Test
    void givenEmptyWordCounts_whenSortIsCalled_thenShouldReturnEmptyMap() {
        Map<String, Integer> input = Map.of();
        Map<String, Integer> result = WordCountUtil.sort(input, 3);

        assertTrue(result.isEmpty());
    }

    @Test
    void givenSingleWordInWordCounts_whenSortIsCalled_thenShouldReturnSingleEntryMap() {
        Map<String, Integer> input = Map.of("apple", 3);
        Map<String, Integer> result = WordCountUtil.sort(input, 3);
        Map<String, Integer> expected = Map.of("apple", 3);

        assertEquals(expected, result);
    }

    @Test
    void givenNullWordCounts_whenSortIsCalled_thenShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> WordCountUtil.sort(null, 3));
    }
}
