package com.webtracer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.webtracer.config.ConfigFileLoader;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.CrawlResult;
import com.webtracer.crawler.CrawlResultSerializer;
import com.webtracer.crawler.GenericWebCrawler;
import com.webtracer.di.module.CrawlerModule;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class App {

    @Inject
    private GenericWebCrawler crawler;

    private final WebCrawlerConfig config;

    private App(WebCrawlerConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public static void main(String[] args) {
        Date start = new Date();

        log.info("""
                 \s
                 /\\__\\       /\\  \\       /\\  \\    /\\  \\       /\\  \\       /\\  \\       /\\  \\       /\\  \\       /\\  \\   \s
                /:/ _/_     /::\\  \\     /::\\  \\   \\:\\  \\     /::\\  \\     /::\\  \\     /::\\  \\     /::\\  \\     /::\\  \\  \s
               /:/ /\\__\\   /:/\\:\\  \\   /:/\\:\\  \\   \\:\\  \\   /:/\\:\\  \\   /:/\\:\\  \\   /:/\\:\\  \\   /:/\\:\\  \\   /:/\\:\\  \\ \s
              /:/ /:/ _/_ /::\\~\\:\\  \\ /::\\~\\:\\__\\  /::\\  \\ /::\\~\\:\\  \\ /::\\~\\:\\  \\ /::\\~\\:\\  \\ /:/  \\:\\  \\ /::\\~\\:\\  \\\s
             /:/_/:/ /\\__/:/\\:\\ \\:\\__/:/\\:\\ \\:|__|/:/\\:\\__/:/\\:\\ \\:\\__/:/\\:\\ \\:\\__/:/\\:\\ \\:\\__/:/__/ \\:\\__/:/\\:\\ \\:\\__\\
             \\:\\/:/ /:/  \\:\\~\\:\\ \\/__\\:\\~\\:\\/:/  /:/  \\/__\\/_|::\\/:/  \\/__\\:\\/:/  \\:\\  \\  \\/__\\:\\~\\:\\ \\/__\\/_|::\\/:/  /
              \\::/_/:/  / \\:\\ \\:\\__\\  \\:\\ \\::/  /:/  /       |:|::/  /     \\::/  / \\:\\  \\      \\:\\ \\:\\__\\    |:|::/  /\s
               \\:\\/:/  /   \\:\\ \\/__/   \\:\\/:/  /\\/__/        |:|\\/__/      /:/  /   \\:\\  \\      \\:\\ \\/__/    |:|\\/__/ \s
                \\::/  /     \\:\\__\\      \\::/__/              |:|  |       /:/  /     \\:\\__\\      \\:\\__\\      |:|  |   \s
                 \\/__/       \\/__/       ~~                   \\|__|       \\/__/       \\/__/       \\/__/       \\|__|  \s
            """);

        if (args.length != 1) {
            log.warn("Path to JSON configuration must be provided as a CLI argument.");
            return;
        }

        try {
            WebCrawlerConfig config = new ConfigFileLoader(Path.of(args[0])).fetchConfig();
            new App(config).run();
            Date end = new Date();
            log.info("Elapsed time was {} ms.", end.getTime() - start.getTime());
        } catch (ApiException e) {
            log.error("An error occurred while fetching the configuration or running the application.", e);
        }
    }

    private void run() throws ApiException {
        log.info("Starting the web crawler application.");

        // Inject dependencies using Guice
        Guice.createInjector(new CrawlerModule(config)).injectMembers(this);
        log.debug("Dependencies injected successfully.");

        // Perform the crawling operation
        CrawlResult result = crawler.crawl(config.getInitialPages());
        log.info("Crawling completed successfully.");

        CrawlResultSerializer<CrawlResult> resultWriter = new CrawlResultSerializer<>(result);

        String resultPath = config.getCrawlResultPath();

        // Handle writing the result to the specified file path or console
        if (resultPath != null && !resultPath.isEmpty()) {
            log.info("Writing crawl results to file: {}", resultPath);
            try (FileWriter fileWriter = new FileWriter(resultPath)) {
                resultWriter.saveToWriter(fileWriter);
                log.info("Crawl results written to file successfully.");
            } catch (IOException e) {
                log.error("Failed to write crawl results to file: {}", resultPath, e);
                throw new ApiException("Failed to write crawl result to file: " + resultPath, e);
            }
        } else {
            log.info("No result path provided, writing crawl results to console.");
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out) {
                @Override
                public void close() throws IOException {
                    // Do not close System.out
                    flush();
                }
            }) {
                resultWriter.saveToWriter(outputStreamWriter);
            } catch (IOException e) {
                log.error("Failed to write crawl results to console.", e);
                throw new ApiException("Failed to write crawl result to console", e);
            }
        }

        log.info("Web crawler application finished.");
    }

}
