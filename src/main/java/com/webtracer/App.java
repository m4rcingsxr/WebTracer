package com.webtracer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.webtracer.config.ConfigFileLoader;
import com.webtracer.config.WebCrawlerConfig;
import com.webtracer.crawler.CrawlResult;
import com.webtracer.crawler.CrawlResultSerializer;
import com.webtracer.crawler.GenericWebCrawler;
import com.webtracer.di.module.CrawlerModule;

import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Objects;

public class App {

    @Inject
    private GenericWebCrawler crawler;

    private final WebCrawlerConfig config;

    private App(WebCrawlerConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            return;
        }

        WebCrawlerConfig config = new ConfigFileLoader(Path.of(args[0])).fetchConfig();
        new App(config).run();
    }

    private void run() throws Exception {
        Guice.createInjector(new CrawlerModule(config)).injectMembers(this);

        CrawlResult result = crawler.crawl(config.getInitialPages());
        CrawlResultSerializer<CrawlResult> resultWriter = new CrawlResultSerializer<>(result);

        String resultPath = config.getCrawlResultPath();
        System.out.println(resultPath);

        if(resultPath != null && !resultPath.isEmpty()) {
            try (FileWriter fileWriter = new FileWriter(resultPath)) {
                resultWriter.saveToWriter(fileWriter);
            }
        } else {
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out)) {
                resultWriter.saveToWriter(outputStreamWriter);
            }
        }
    }

}
