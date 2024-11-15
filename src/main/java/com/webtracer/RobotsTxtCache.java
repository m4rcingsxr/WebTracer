package com.webtracer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class RobotsTxtCache {

    private static final Logger log = Logger.getLogger(RobotsTxtCache.class.getName());
    private final ConcurrentMap<String, RobotsTxtRules> domainRulesCache = new ConcurrentHashMap<>();
    private final String userAgent;

    public RobotsTxtCache(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Checks if the specified URI is allowed to be crawled by checking the robots.txt rules.
     * The rules are cached per domain for efficient reuse.
     *
     * @param uri the URI to check
     * @return true if the URI is allowed to be crawled, false otherwise
     */
    public boolean isAllowed(URI uri) {
        String domainKey = uri.getScheme() + "://" + uri.getHost();

        // Fetch rules if not already cached
        RobotsTxtRules rules = domainRulesCache.computeIfAbsent(domainKey, key -> fetchRobotsTxtRules(uri));

        // Check the rules for the specific path
        return rules.isAllowed(uri.getPath());
    }

    /**
     * Fetches and parses the robots.txt file for the given URI's domain.
     *
     * @param uri the URI to fetch rules for
     * @return the parsed rules for the domain
     */
    private RobotsTxtRules fetchRobotsTxtRules(URI uri) {
        String robotsTxtUrl = uri.getScheme() + "://" + uri.getHost() + "/robots.txt";
        log.info("Fetching robots.txt from: " + robotsTxtUrl);

        List<String> disallowedPaths = new ArrayList<>();
        List<String> allowedPaths = new ArrayList<>();
        try {
            Document robotsTxtDoc = Jsoup.connect(robotsTxtUrl).get();
            String robotsTxtContent = robotsTxtDoc.body().text();

            // Ensure structured processing by splitting content into lines
            robotsTxtContent = robotsTxtContent
                    .replaceAll("User-agent:", "\nUser-agent:")
                    .replaceAll("Disallow:", "\nDisallow:")
                    .replaceAll("Allow:", "\nAllow:");

            String[] lines = robotsTxtContent.split("\n");
            boolean isRelevantSection = false;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;  // Ignore empty lines and comments

                if (line.toLowerCase().startsWith("user-agent:")) {
                    String specifiedUserAgent = line.substring("user-agent:".length()).trim().toLowerCase();
                    isRelevantSection = specifiedUserAgent.equals(userAgent.toLowerCase()) || specifiedUserAgent.equals("*");
                } else if (isRelevantSection) {
                    if (line.toLowerCase().startsWith("disallow:")) {
                        String path = line.substring("disallow:".length()).trim();
                        if (!path.isEmpty()) disallowedPaths.add(path);
                    } else if (line.toLowerCase().startsWith("allow:")) {
                        String path = line.substring("allow:".length()).trim();
                        if (!path.isEmpty()) allowedPaths.add(path);
                    }
                }
            }
        } catch (IOException e) {
            log.warning("Failed to fetch or parse robots.txt: " + e.getMessage());
        }

        return new RobotsTxtRules(allowedPaths, disallowedPaths);
    }

    /**
     * Stores the parsed rules from a robots.txt file.
     */
    private static class RobotsTxtRules {
        private final List<String> allowedPaths;
        private final List<String> disallowedPaths;

        public RobotsTxtRules(List<String> allowedPaths, List<String> disallowedPaths) {
            this.allowedPaths = allowedPaths;
            this.disallowedPaths = disallowedPaths;
        }

        /**
         * Checks if the given path is allowed based on the robots.txt rules.
         *
         * @param path the path to check
         * @return true if the path is allowed, false otherwise
         */
        public boolean isAllowed(String path) {
            for (String allowed : allowedPaths) {
                if (path.startsWith(allowed)) return true;
            }
            for (String disallowed : disallowedPaths) {
                if (path.startsWith(disallowed)) return false;
            }
            return true;  // Default to allowing if no matching rules are found
        }
    }
}
