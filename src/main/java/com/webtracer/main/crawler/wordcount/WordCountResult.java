package com.webtracer.main.crawler.wordcount;

import com.webtracer.main.crawler.CrawlResult;

/**
 * This final class represents the result of a web crawling operation that is specifically
 * focused on counting words across the crawled web pages. It implements the {@link CrawlResult}
 * interface, providing a concrete representation of the crawl outcome with word count data.
 *
 * The {@link WordCountResult} class is designed to be immutable, ensuring that the results
 * of the crawl cannot be modified after they have been created. This class can contain
 * information such as the total word count, the word frequency distribution, and any other
 * relevant data extracted during the crawl.
 *
 * As it implements the {@link CrawlResult} interface, instances of this class can be returned
 * by the `crawl` method in the {@link com.webtracer.main.crawler.wordcount.WordCountWebCrawler} interface, providing a structured
 * way to access the results of a word count-focused web crawl.
 */
public final class WordCountResult implements CrawlResult {



}
