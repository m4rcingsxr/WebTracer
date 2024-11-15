# WebTracer

<hr/>

WebTracerCrawler is a web crawler capable of counting words on crawled web pages. It supports configurable
crawling depth, URL exclusion patterns, domain throttling to avoid overwhelming servers, and word count filtering. Users
can flexibly control crawling behavior through a JSON configuration file, including the ability to change the underlying
web crawler implementation.



### Project Purpose and Goals
<hr/>


WebTracerCrawler's primary goal is to efficiently and ethically crawl web pages while providing flexibility for various
data extraction, analysis, and research tasks.


### Technologies
<hr/>


- Java 11
- Maven
- Jsoup
- Google Guice
- Jackson
- Lombok
- Junit5, Mockito
- Logback, SLF4J

### Installation and Setup
<hr/>

#### Web Crawler Configuration Parameters
- ***initialPages*** 
    > The initial pages where the web crawler will begin crawling.
- ***excludedWords*** 
    > A list of regular expressions defining words to exclude from the word count. For example, "^.{1,3}$" will exclude words of length 1 to 3 characters.
- ***customImplementation*** 
    > Specifies a custom implementation class for the web crawler. The class should implement the crawler logic and be fully qualified.
- ***timeoutSeconds*** 
    > The maximum allowed duration for the crawler's operation, in seconds. Once this time limit is reached, the crawler will stop fetching new pages.
- ***popularWordCount*** 
    > Specifies how many of the most frequently occurring words to include in the result. Useful for generating summaries or identifying popular words.
- ***mapDepth*** 
    > Controls the maximum depth to which the crawler will follow links. A value of 1 means only the initial pages will be crawled, while higher values allow deeper exploration.
- ***concurrencyLevel*** 
    > Specifies the level of concurrency for the web crawling operation. A value of -1 indicates that the number of available CPU cores should be used for optimal parallelism.
- ***throttleDelayMillis*** 
    > The delay (in milliseconds) between HTTP requests to the same domain. Helps in preventing overloading or being blocked by a server due to too many requests in a short time.

#### Example Configurations

- Basic config
    ```json
      {
          "initialPages": ["https://example.com"],
          "excludedWords": ["^.{1,3}$"],
          "customImplementation": "com.webtracer.crawler.wordcount.RecursiveTaskWebCrawler",
          "popularWordCount": 5,
          "maxDepth": 4,
          "concurrencyLevel": -1,
          "throttleDelayMillis": 500
    }


    ```

- Test robots.txt
    ```json
    {
      "initialPages": ["https://google.com/search", "https://google.com/pqa", "https://google.com/default", "https://google.com/groups", "https://google.com/sdch"],
      "excludedWords": ["^.{1,3}$"],
      "customImplementation" : "com.webtracer.crawler.wordcount.SequentialWebCrawler",
      "timeoutSeconds": 100,
      "popularWordCount": 5,
      "maxDepth": 2,
      "concurrencyLevel" : -1,
      "throttleDelayMillis" : 500
    }
    
    ```
  
#### Prerequisites
- Java 11+
- Maven 3.6+

#### Build & Run
1. Clone the repository
   ```shell
    git clone https://github.com/yourusername/WebTracerCrawler.git
    cd WebTracerCrawler
   ```
2. Build with Maven
    ```shell
    mvn clean package
    ```
3. Run the application
    ```shell
    java -jar target/WebTracerCrawler-1.0.jar src/main/resources/base_cfg.json
    ```
   
### Features
<hr/>

- Customizable Crawling: Configure depth, concurrency, and URL patterns.
- Word Counting: Extract word counts from web pages with filtering.
- Domain Throttling: Prevent overwhelming target domains.
- Custom Implementations: Easily change underlying crawler logic.
