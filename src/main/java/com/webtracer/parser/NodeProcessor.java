package com.webtracer.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.net.URI;

public interface NodeProcessor {

    void processNode(Node node, int depth);

    void processTextNode(TextNode textNode);

    void processElement(Element element);

    String resolveLink(Element element);

    boolean isLocalUri(URI uri);

    ParseResult getResult();

}
