package com.webtracer.parser;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * The {@code ParseResult} interface serves as a marker interface for classes that represent
 * the result of a page parsing operation. Classes implementing this interface should encapsulate
 * the data and outcomes derived from parsing a web page or document.
 *
 * <p>It is intended to be extended by more specific result classes that contain relevant data
 * according to the type of parsing performed.</p>
 */
public interface ParseResult {
}