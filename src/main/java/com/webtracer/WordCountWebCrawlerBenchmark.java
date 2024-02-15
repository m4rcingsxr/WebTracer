package com.webtracer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.wordcount.RecursiveActionWebCrawler;
import com.webtracer.crawler.wordcount.RecursiveTaskWebCrawler;
import com.webtracer.crawler.wordcount.WordCountResult;
import com.webtracer.di.module.CrawlerModule;
import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class WordCountWebCrawlerBenchmark {

    private RecursiveActionWebCrawler recursiveActionWebCrawler;
    private RecursiveTaskWebCrawler recursiveTaskWebCrawler;
    private final List<String> initialPages = List.of("https://blog.udacity.com");

    @Setup(Level.Trial)
    public void setup() {}

    // Benchmark 1: Depth 2, 1,000,000 words, 10-minute timeout
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)

    public WordCountResult benchmarkRecursiveActionWebCrawler_1MWords() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 10);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_1MWords() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 10);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    // Benchmark 2: Depth 2, 10 words, 10-minute timeout
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_10Words() throws ApiException {
        setupWebCrawlerConfig(2, 10, Duration.ofMinutes(10), 10);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_10Words() throws ApiException {
        setupWebCrawlerConfig(2, 10, Duration.ofMinutes(10), 10);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    // Benchmark 3: Depth 1, High Breadth, Short Timeout
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_HighBreadthShortTimeout() throws ApiException {
        setupWebCrawlerConfig(1, 1_000_000, Duration.ofSeconds(5), 10);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_HighBreadthShortTimeout() throws ApiException {
        setupWebCrawlerConfig(1, 1_000_000, Duration.ofSeconds(5), 10);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    // Benchmark 4: Depth 5, Low Breadth, Long Timeout
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_DeepDepthLongTimeout() throws ApiException {
        setupWebCrawlerConfig(5, 100, Duration.ofMinutes(30), 10);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_DeepDepthLongTimeout() throws ApiException {
        setupWebCrawlerConfig(5, 100, Duration.ofMinutes(30), 10);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    // Benchmark 5: Low Concurrency
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_LowConcurrency() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 2);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_LowConcurrency() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 2);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    // Benchmark 6: High Concurrency
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_HighConcurrency() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 24);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_HighConcurrency() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 24);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_Parallel() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), -1);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_Parallel() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), -1);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    // Benchmark 7: Exclude URLs (like images)
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveActionWebCrawler_WithExclusions() throws ApiException {
        setupWebCrawlerConfigWithExclusions(2, 1_000_000, Duration.ofMinutes(10), -1);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(1)
    public WordCountResult benchmarkRecursiveTaskWebCrawler_WithExclusions() throws ApiException {
        setupWebCrawlerConfigWithExclusions(2, 1_000_000, Duration.ofMinutes(10), -1);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    // Benchmark 8: Limited Heap Size to Trigger GC Overhead
    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(value = 1, jvmArgs = {"-Xmx256m"})
    public WordCountResult benchmarkRecursiveActionWebCrawler_LimitedHeap() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 10);
        return recursiveActionWebCrawler.crawl(initialPages);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(value = 1, jvmArgs = {"-Xmx256m"})
    public WordCountResult benchmarkRecursiveTaskWebCrawler_LimitedHeap() throws ApiException {
        setupWebCrawlerConfig(2, 1_000_000, Duration.ofMinutes(10), 10);
        return recursiveTaskWebCrawler.crawl(initialPages);
    }

    private void setupWebCrawlerConfig(int depth, int popularWordCount, Duration timeout, int concurrencyLevel) {
        WebCrawlerConfig config = WebCrawlerConfig.builder()
                .maxDepth(depth)
                .popularWordCount(popularWordCount)
                .timeout(timeout)
                .excludedUrls(List.of())
                .concurrencyLevel(concurrencyLevel)
                .throttleDelayMillis(500)
                .build();

        Injector injector = Guice.createInjector(new CrawlerModule(config));
        recursiveActionWebCrawler = injector.getInstance(RecursiveActionWebCrawler.class);
        recursiveTaskWebCrawler = injector.getInstance(RecursiveTaskWebCrawler.class);
    }

    private void setupWebCrawlerConfigWithExclusions(int depth, int popularWordCount, Duration timeout, int concurrencyLevel) {
        WebCrawlerConfig config = WebCrawlerConfig.builder()
                .maxDepth(depth)
                .popularWordCount(popularWordCount)
                .timeout(timeout)
                .excludedUrls(List.of(Pattern.compile(".*\\.jpg$"), Pattern.compile(".*\\.png$"), Pattern.compile(".*\\.gif$")))
                .concurrencyLevel(concurrencyLevel)
                .throttleDelayMillis(500)
                .build();

        Injector injector = Guice.createInjector(new CrawlerModule(config));
        recursiveActionWebCrawler = injector.getInstance(RecursiveActionWebCrawler.class);
        recursiveTaskWebCrawler = injector.getInstance(RecursiveTaskWebCrawler.class);
    }
}
