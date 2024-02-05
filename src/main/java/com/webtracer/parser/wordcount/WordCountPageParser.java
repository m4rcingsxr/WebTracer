package com.webtracer.parser.wordcount;

import com.webtracer.parser.PageParser;

/**
 * The WordCountPageParser interface extends the {@link PageParser} interface,
 * specifying that the parser will return a {@link WordCountParseResult} object.
 *
 * This interface is used to define the contract for page parsers that focus on
 * extracting word counts and hyperlinks from web pages. Implementations of this
 * interface will provide the logic for parsing web pages and returning the results
 * as a {@link WordCountParseResult}.
 */
interface WordCountPageParser extends PageParser {

    /**
     * Parses a web page and extracts word count data along with hyperlinks.
     *
     * @return a {@link WordCountParseResult} object containing the word frequency map and the
     *         list of hyperlinks found on the page.
     */
    @Override
    WordCountParseResult parse();

}