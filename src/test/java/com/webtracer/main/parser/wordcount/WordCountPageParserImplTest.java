package com.webtracer.main.parser.wordcount;

import com.webtracer.main.parser.DocumentLoader;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WordCountPageParserImplTest {

    private DocumentLoader documentLoaderMock;
    private List<Pattern> excludePatterns;
    private String pageUri;

    @BeforeEach
    void setUp() {
        documentLoaderMock = mock(DocumentLoader.class);
        excludePatterns = List.of(Pattern.compile("\\d+")); // Example: Exclude numbers
        pageUri = "file:///test-page.html";
    }

    @Test
    void givenValidDocument_whenParse_thenReturnsCorrectWordCountParseResult() throws IOException {
        // Given
        Document documentMock = mock(Document.class);
        when(documentLoaderMock.loadDocument(any(URI.class))).thenReturn(Optional.of(documentMock));

        WordCountPageParserImpl parser = new WordCountPageParserImpl(pageUri, excludePatterns, documentLoaderMock);

        // When
        WordCountParseResult result = parser.parse();

        // Then
        verify(documentMock).traverse(any());
        assertNotNull(result);
    }

    @Test
    void givenIOExceptionDuringDocumentLoading_whenParse_thenReturnsEmptyResult() throws IOException {
        // Given
        when(documentLoaderMock.loadDocument(any(URI.class))).thenThrow(new IOException());

        WordCountPageParserImpl parser = new WordCountPageParserImpl(pageUri, excludePatterns, documentLoaderMock);

        // When
        WordCountParseResult result = parser.parse();

        // Then
        assertTrue(result.getWordFrequencyMap().isEmpty());
        assertTrue(result.getHyperLinkList().isEmpty());
    }

    @Test
    void givenEmptyDocument_whenParse_thenReturnsEmptyResult() throws IOException {
        // Given
        Document emptyDocument = mock(Document.class);
        when(documentLoaderMock.loadDocument(any(URI.class))).thenReturn(Optional.of(emptyDocument));

        WordCountPageParserImpl parser = new WordCountPageParserImpl(pageUri, excludePatterns, documentLoaderMock);

        // When
        WordCountParseResult result = parser.parse();

        // Then
        assertTrue(result.getWordFrequencyMap().isEmpty());
        assertTrue(result.getHyperLinkList().isEmpty());
    }

    @Test
    void givenNullDocument_whenParse_thenReturnsEmptyResult() throws IOException {
        // Given
        when(documentLoaderMock.loadDocument(any(URI.class))).thenReturn(Optional.empty());

        WordCountPageParserImpl parser = new WordCountPageParserImpl(pageUri, excludePatterns, documentLoaderMock);

        // When
        WordCountParseResult result = parser.parse();

        // Then
        assertTrue(result.getWordFrequencyMap().isEmpty());
        assertTrue(result.getHyperLinkList().isEmpty());
    }
}
