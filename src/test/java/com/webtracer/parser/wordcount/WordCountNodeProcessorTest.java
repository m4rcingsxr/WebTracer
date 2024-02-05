package com.webtracer.parser.wordcount;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Evaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WordCountNodeProcessorTest {

    private WordCountParseResult.Builder resultBuilderMock;
    private List<Pattern> excludePatterns;
    private URI pageUri;

    @BeforeEach
    void setUp() {
        resultBuilderMock = mock(WordCountParseResult.Builder.class);
        excludePatterns = List.of(Pattern.compile("\\d+")); // Example: Exclude numbers
        pageUri = URI.create("file:///test-page.html");
    }

    @Test
    void givenTextNode_whenProcessTextNode_thenWordsAreAddedToResultBuilder() {
        // Given
        TextNode textNode = new TextNode("Hello World 123");
        WordCountNodeProcessor processor = new WordCountNodeProcessor(excludePatterns, resultBuilderMock, pageUri);

        // When
        processor.processTextNode(textNode);

        // Then
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        verify(resultBuilderMock, times(2)).addWord(wordCaptor.capture());
        List<String> capturedWords = wordCaptor.getAllValues();
        assertEquals(2, capturedWords.size());
        assertTrue(capturedWords.contains("hello"));
        assertTrue(capturedWords.contains("world"));
    }

    @Test
    void givenElementWithHref_whenProcessElement_thenLinkIsAddedToResultBuilder() {
        // Given
        Element element = mock(Element.class);
        when(element.is(any(Evaluator.Tag.class))).thenReturn(true);
        when(element.hasAttr("href")).thenReturn(true);
        when(element.attr("href")).thenReturn("/relative-link.html");

        WordCountNodeProcessor processor = new WordCountNodeProcessor(excludePatterns, resultBuilderMock, pageUri);

        // When
        processor.processElement(element);

        // Then
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(resultBuilderMock).addLink(linkCaptor.capture());
        assertEquals("file:///relative-link.html", linkCaptor.getValue());
    }

    @Test
    void givenRemoteHref_whenResolveLink_thenAbsoluteUrlIsReturned() {
        // Given
        Element element = mock(Element.class);
        when(element.attr("href")).thenReturn("https://example.com/page");

        WordCountNodeProcessor processor = new WordCountNodeProcessor(excludePatterns, resultBuilderMock, pageUri);

        // When
        String resolvedLink = processor.resolveLink(element);

        // Then
        assertEquals("https://example.com/page", resolvedLink);
    }

    @Test
    void givenLocalFileHref_whenResolveLink_thenFileUriIsReturned() {
        // Given
        Element element = mock(Element.class);
        when(element.attr("href")).thenReturn("/local-page.html");

        WordCountNodeProcessor processor = new WordCountNodeProcessor(excludePatterns, resultBuilderMock, pageUri);

        // When
        String resolvedLink = processor.resolveLink(element);

        // Then
        assertEquals("file:///local-page.html", resolvedLink);
    }

}
