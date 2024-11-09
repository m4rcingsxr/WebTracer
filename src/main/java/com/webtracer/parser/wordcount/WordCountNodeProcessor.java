package com.webtracer.parser.wordcount;

import com.webtracer.parser.NodeProcessor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * <p> This class is intended to be used as a helper component in a larger HTML parsing and analysis process,
 * where it contributes to the creation of a {@link WordCountParseResult} that encapsulates the results of the analysis.</p>
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
@Slf4j
final class WordCountNodeProcessor implements NodeProcessor {

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
     * {@link #processTextNode(TextNode)} for text nodes or {@link #processElement(Element)} for elements.
     *
     * @param node  The node being processed.
     * @param depth The depth of the node in the document tree (unused in this implementation).
     */
    @Override
    public void processNode(Node node, int depth) {
        log.trace("Processing node at depth {}: {}", depth, node.nodeName());
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
    @Override
    public void processTextNode(TextNode textNode) {
        String text = textNode.text().strip();
        log.trace("Processing text node: {}", text);

        Arrays.stream(WHITESPACE_PATTERN.split(text))
                .filter(s -> !s.isBlank())
                .filter(s -> excludeWordPatterns.stream().noneMatch(p -> p.matcher(s).matches()))
                .map(s -> NON_WORD_PATTERN.matcher(s).replaceAll(""))
                .map(String::toLowerCase)
                .forEach(word -> {
                    log.trace("Adding word to result: {}", word);
                    resultBuilder.addWord(word);
                });
    }

    /**
     * Processes an element to extract hyperlinks. If the element is an anchor tag with an href attribute,
     * the link is resolved and added to the result builder.
     *
     * @param element The element to process.
     */
    @Override
    public void processElement(Element element) {
        log.trace("Processing element: {}", element.tagName());
        if (element.is(new Evaluator.Tag("a")) && element.hasAttr("href")) {
            String link = resolveLink(element);
            log.trace("Resolved hyperlink: {}", link);
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
    @Override
    public String resolveLink(Element element) {
        String href = element.attr("href");

        if (href.startsWith("http://") || href.startsWith("https://")) {
            log.trace("Returning fully qualified URL: {}", href);
            return href;
        }

        if (isLocalUri(pageUri)) {
            Path basePath = Path.of(pageUri).getParent();
            String resolvedLink = href.startsWith("/")
                    ? basePath.resolve(href.substring(1)).toUri().toString()
                    : basePath.resolve(href).toUri().toString();
            log.trace("Resolved local file URL: {}", resolvedLink);
            return resolvedLink;
        } else {
            String resolvedLink = element.attr("abs:href");
            log.trace("Resolved remote URL: {}", resolvedLink);
            return resolvedLink;
        }
    }

    /**
     * Checks if the provided {@link URI} represents a local file. This helps in determining how to resolve the link.
     *
     * @param uri The URI to check.
     * @return true if the URI represents a local file, false otherwise.
     */
    @Override
    public boolean isLocalUri(URI uri) {
        boolean isLocal = "file".equals(uri.getScheme());
        log.trace("URI {} is local: {}", uri, isLocal);
        return isLocal;
    }

    /**
     * Returns the final result after processing all nodes. This result includes word frequencies and hyperlinks.
     *
     * @return The final result as a {@link WordCountParseResult}.
     */
    @Override
    public WordCountParseResult getResult() {
        log.debug("Building final WordCountParseResult");
        return resultBuilder.build();
    }
}
