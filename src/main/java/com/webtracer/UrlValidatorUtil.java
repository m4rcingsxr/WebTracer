package com.webtracer;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.logging.Logger;

public class UrlValidatorUtil {

    private static final Logger log = Logger.getLogger(UrlValidatorUtil.class.getName());

    // Apache Commons UrlValidator instance with default options (validates http, https)
    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"http", "https", "file"});

    /**
     * Validates if the given URL is a valid web page URL for crawling.
     *
     * @param url the URL string to validate
     * @return true if the URL is valid for web crawling, false otherwise
     */
    public static boolean isValidWebPageUrl(String url) {
        if (!URL_VALIDATOR.isValid(url)) {
            log.warning("Invalid URL: " + url);
            return false;
        }

        // Additional custom checks (e.g., excluding specific file extensions)
        String[] fileExtensionsToExclude = {
                ".jpg", ".png", ".pdf", ".zip", ".exe", ".mp4", ".mp3", ".doc", ".docx", ".xls", ".xlsx"
        };
        for (String extension : fileExtensionsToExclude) {
            if (url.toLowerCase().endsWith(extension)) {
                log.warning("URL points to a file, skipping: " + url);
                return false;
            }
        }

        return true;
    }
}
