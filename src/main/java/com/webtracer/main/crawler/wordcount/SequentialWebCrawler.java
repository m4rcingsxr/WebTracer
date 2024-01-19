package com.webtracer.main.crawler.wordcount;

import com.webtracer.main.parser.wordcount.WordCountPageParserFactoryImpl;
import com.webtracer.main.parser.wordcount.WordCountParseResult;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class SequentialWebCrawler implements WordCountWebCrawler {

    private final Clock clock;
    private final Duration crawlTimeout;
    private final int maxDepth;
    private final List<Pattern> excludedUrls;
    private final WordCountPageParserFactoryImpl parserFactory;

    @Override
    public WordCountResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant().plus(crawlTimeout);
        Map<String, Integer> counts = new HashMap<>();
        Set<String> visitedUrls = new HashSet<>();
        for (String url : startingUrls) {
            crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
        }

        return WordCountResult.builder()
                .wordFrequencyMap(counts)
                .totalUrlsVisited(visitedUrls.size())
                .build();
    }

    private void crawlInternal(
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : excludedUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);
        WordCountParseResult result = parserFactory.createParserInstance(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordFrequencyMap().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }
        for (String link : result.getHyperLinkList()) {
            crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
        }
    }

}
