package com.webtracer.main.parser;

/**
 * The GenericPageParser interface serves as a base interface for parsing web pages.
 * It is intended to be extended by other specific page parser interfaces that define
 * methods for extracting and processing data from web pages.
 */
public interface GenericPageParser<T> {

    /**
     * Parses a web page and extracts relevant data.
     *
     * @return a parsed representation of the web page, defined by the generic type T.
     *         This could be any crawler response.
     */
    T parse();

}