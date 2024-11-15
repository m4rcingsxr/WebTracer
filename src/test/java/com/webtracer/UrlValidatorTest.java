package com.webtracer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

 class UrlValidatorTest {

    @ParameterizedTest(name = "Given a valid URL \"{0}\", when checked, then it should be valid")
    @CsvSource({
            "http://www.example.com",
            "https://www.example.com",
            "https://example.com/page",
            "http://example.com:8080/test",
            "https://www.example.com?q=search",
            "http://subdomain.example.com/path"
    })
    void givenValidUrl_whenChecked_thenShouldBeValid(String url) {
        assertTrue(UrlValidatorUtil.isValidWebPageUrl(url));
    }

    @ParameterizedTest(name = "Given an invalid URL \"{0}\", when checked, then it should be invalid")
    @CsvSource({
            "ftp://example.com",               // Unsupported scheme
            "file:///path/to/file",           // Unsupported scheme
            "http://",                        // Missing host
            "https://.com",                   // Invalid host
            "http://example.com/file.pdf",    // Disallowed file extension
            "https://example.com/file.exe",   // Disallowed file extension
            "example.com",                    // Missing scheme
            "https://example.invalidformat"   // Invalid domain
    })
    void givenInvalidUrl_whenChecked_thenShouldBeInvalid(String url) {
        assertFalse(UrlValidatorUtil.isValidWebPageUrl(url));
    }
}