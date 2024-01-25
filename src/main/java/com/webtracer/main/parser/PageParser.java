package com.webtracer.main.parser;

import java.io.IOException;

/**
 * The PageParser interface defines the contract for classes that are responsible for parsing web pages.
 * Implementations of this interface should provide the logic for extracting data from web pages
 * and returning the results encapsulated in a {@link ParseResult} object.
 */
public interface PageParser {

    /**
     * Parses a web page and extracts relevant data.
     *
     * @return a {@link ParseResult} object containing the results of the parsing operation.
     *         The exact type of the returned result depends on the implementation.
     */
    ParseResult parse() throws IOException;

}