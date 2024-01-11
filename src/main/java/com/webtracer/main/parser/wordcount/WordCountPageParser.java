package com.webtracer.main.parser.wordcount;

import com.webtracer.main.parser.GenericPageParser;

/**
 * The WordCountPageParser interface extends the GenericPageParser interface,
 * specifying that the parser will return a {@link WordCountParseResult} object.
 *
 * This interface is used to define the contract for page parsers that focus on
 * extracting word counts and hyperlinks from web pages. Implementations of this
 * interface will provide the logic for parsing web pages and returning the results
 * as a {@link WordCountParseResult}.
 */
public interface WordCountPageParser extends GenericPageParser<WordCountParseResult> {

}
