package com.webtracer.main.crawler.wordcount;

import com.webtracer.main.crawler.GenericWebCrawler;

import java.util.List;

/**
 * This interface extends the {@link GenericWebCrawler} interface, specifically for web crawlers
 * that are designed to perform word count operations across web pages. The {@link WordCountWebCrawler}
 * interface defines a specialized `crawl` method that returns a {@link WordCountResult} object,
 * encapsulating the results of the word count operation.
 *
 * Implementations of this interface should focus on how to efficiently count words across the
 * provided list of starting URLs, including how to handle different types of content, manage
 * links, and aggregate the word count data.
 */
interface WordCountWebCrawler extends GenericWebCrawler {

  /**
   * Starts the web crawling process using the provided list of starting URLs, specifically
   * focusing on counting words on the crawled web pages. The result of this crawl is returned
   * as a {@link WordCountResult} object, which includes detailed word count data.
   *
   * @param startingUrls a list of URLs to start the crawling process from. These URLs serve as the
   *                     entry points for the crawler.
   * @return a {@link WordCountResult} object containing the outcome of the crawl, including
   *         the word count data and any errors encountered during the process.
   */
  @Override
  WordCountResult crawl(List<String> startingUrls);

}
