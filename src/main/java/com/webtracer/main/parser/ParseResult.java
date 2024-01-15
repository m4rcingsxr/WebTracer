package com.webtracer.main.parser;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * The ParseResult class serves as a base class for storing the results of a page parsing operation.
 * It contains a list of hyperlinks found on the parsed page.
 * Subclasses should extend this class to include additional data relevant to their specific parsing tasks.
 */
@Getter
@RequiredArgsConstructor
public abstract class ParseResult {

    /**
     * A list of hyperlinks extracted from the parsed web page.
     */
    @NonNull
    private final List<String> hyperLinkList;

}

