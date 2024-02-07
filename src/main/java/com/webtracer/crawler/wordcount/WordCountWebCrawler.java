package com.webtracer.crawler.wordcount;

import com.webtracer.ApiException;
import com.webtracer.crawler.GenericWebCrawler;

import java.util.List;


/**
 * The {@link WordCountWebCrawler} interface extends the {@link GenericWebCrawler} interface,
 * specifically tailored for web crawlers that perform word count operations across web pages.
 * <p>
 * Implementations of this interface should focus on how to efficiently count words across the
 * provided list of starting URLs, including handling different types of content, managing
 * links, and aggregating word count data. The {@code crawl} method returns a {@link WordCountResult}
 * object, encapsulating the results of the word count operation.
 * </p>
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
   * @throws ApiException if an error occurs during the crawling process, such as network issues or
   *                      unexpected response formats.
   */
  @Override
  WordCountResult crawl(List<String> startingUrls) throws ApiException;

}
