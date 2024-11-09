package com.webtracer.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.net.URI;

/**
 * The {@code NodeProcessor} interface defines the contract for processing nodes in an HTML document.
 * Implementations of this interface are expected to handle different types of nodes (e.g., text nodes,
 * elements) and perform specific operations such as extracting text or resolving hyperlinks.
 */
public interface NodeProcessor {

    /**
     * Processes a node in the HTML document.
     *
     * @param node  The node being processed.
     * @param depth The depth of the node in the document tree.
     */
    void processNode(Node node, int depth);

    /**
     * Processes a text node in the HTML document.
     *
     * @param textNode The text node to process.
     */
    void processTextNode(TextNode textNode);

    /**
     * Processes an element in the HTML document.
     *
     * @param element The element to process.
     */
    void processElement(Element element);

    /**
     * Resolves a hyperlink in an element to its absolute URL.
     *
     * @param element The element containing the hyperlink.
     * @return The resolved absolute URL.
     */
    String resolveLink(Element element);

    /**
     * Checks if the given URI represents a local file.
     *
     * @param uri The URI to check.
     * @return true if the URI is a local file, false otherwise.
     */
    boolean isLocalUri(URI uri);

    /**
     * Returns the final result after processing all nodes.
     *
     * @return The final result as a {@link ParseResult}.
     */
    ParseResult getResult();
}