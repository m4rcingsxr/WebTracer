package com.webtracer.parser.wordcount;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Evaluator;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code WordCountNodeProcessor} class is responsible for processing HTML nodes to extract
 * words and hyperlinks. It is designed to work in conjunction with a document parser to analyze
 * the content of HTML pages, counting word frequencies and resolving hyperlinks.
 *
 * <p> The {@code WordCountNodeProcessor} is intended to be used as a helper class in the context
 * of a larger HTML parsing and analysis process, where it contributes to the creation of a
 * {@link WordCountParseResult} that encapsulates the results of the analysis.</p>
 *
 * <p> Example usage:
 * <pre>
 *     WordCountParseResult.Builder builder = new WordCountParseResult.Builder();
 *     WordCountNodeProcessor processor = new WordCountNodeProcessor(excludePatterns, builder, pageUri);
 *     document.traverse(processor::processNode);
 *     WordCountParseResult result = processor.getResult();
 * </pre>
 * </p>
 *
 * <p> The class is not designed to be used independently; rather, it is a utility component
 * within a broader HTML parsing framework.</p>
 */
@RequiredArgsConstructor
final class WordCountNodeProcessor {

    /**
     * Pattern to match whitespace characters. This pattern is used to split text into words.
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Pattern to match non-word characters. This pattern is used to remove non-word characters from text.
     */
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("\\W");

    /**
     * List of patterns to exclude certain words from the word count. For example, patterns to exclude numbers.
     */
    @NonNull
    private final List<Pattern> excludeWordPatterns;

    /**
     * Builder object used to accumulate the results, including word frequencies and hyperlinks.
     */
    @NonNull
    private final WordCountParseResult.Builder resultBuilder;

    /**
     * The URI of the page being processed. This is used for resolving relative hyperlinks.
     */
    @NonNull
    private final URI pageUri;

    /**
     * Processes a node in the HTML document. Depending on the type of node, it delegates to either
     * processTextNode() for text nodes or processElement() for elements.
     *
     * @param node The node being processed.
     * @param depth The depth of the node in the document tree (unused in this implementation).
     */
    void processNode(Node node, int depth) {
        if (node instanceof TextNode textNode) {
            processTextNode(textNode);
        } else if (node instanceof Element element) {
            processElement(element);
        }
    }

    /**
     * Processes a text node to extract words and update the word frequency map. The text is split into words,
     * filtered by the exclude patterns, and each valid word is added to the result builder.
     *
     * @param textNode The text node to process.
     */
    void processTextNode(TextNode textNode) {
        // Strip leading and trailing whitespace from the text
        String text = textNode.text().strip();

        // Split the text into words, filter them, clean them up, and add to the result
        Arrays.stream(WHITESPACE_PATTERN.split(text))
                .filter(s -> !s.isBlank()) // Filter out blank strings
                .filter(s -> excludeWordPatterns.stream().noneMatch(p -> p.matcher(s).matches())) // Apply exclusion patterns
                .map(s -> NON_WORD_PATTERN.matcher(s).replaceAll("")) // Remove non-word characters
                .map(String::toLowerCase) // Convert words to lowercase
                .forEach(resultBuilder::addWord); // Add each word to the result builder
    }

    /**
     * Processes an element to extract hyperlinks. If the element is an anchor tag with an href attribute,
     * the link is resolved and added to the result builder.
     *
     * @param element The element to process.
     */
    void processElement(Element element) {
        // Check if the element is an anchor tag (<a>) with an href attribute
        if (element.is(new Evaluator.Tag("a")) && element.hasAttr("href")) {
            // Resolve the link and add it to the result
            String link = resolveLink(element);
            resultBuilder.addLink(link);
        }
    }

    /**
     * Resolves a hyperlink in an element, handling both local and remote URIs. It returns the absolute URL
     * of the hyperlink.
     *
     * @param element The element containing the hyperlink.
     * @return The resolved absolute URL of the hyperlink.
     */
    String resolveLink(Element element) {
        String href = element.attr("href");

        // If href is a fully qualified URL (starts with http://, https://), return it as is
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }

        // Handle local file URLs
        if (isLocalUri(pageUri)) {
            Path basePath = Path.of(pageUri).getParent();
            if (href.startsWith("/")) {
                // Convert absolute paths to file URIs
                return basePath.resolve(href.substring(1)).toUri().toString();
            } else {
                // Convert relative paths to file URIs
                return basePath.resolve(href).toUri().toString();
            }
        } else {
            // For remote URIs, let Jsoup resolve relative URLs
            return element.attr("abs:href");
        }
    }

    /**
     * Checks if the provided {@link URI} represents a local file. This helps in determining how to resolve the link.
     *
     * @param uri The URI to check.
     * @return true if the URI represents a local file, false otherwise.
     */
    boolean isLocalUri(URI uri) {
        return "file".equals(uri.getScheme());
    }

    /**
     * Returns the final result after processing all nodes. This result includes word frequencies and hyperlinks.
     *
     * @return The final result as a {@link WordCountParseResult}.
     */
    WordCountParseResult getResult() {
        return resultBuilder.build();
    }
}
