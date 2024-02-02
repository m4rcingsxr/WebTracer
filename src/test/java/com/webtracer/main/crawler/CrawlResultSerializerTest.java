package com.webtracer.main.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CrawlResultSerializerTest {

    @TempDir
    Path tempDir;


    static class MockCrawlResult extends CrawlResult {
        public String status = "success";
        public int pagesCrawled = 10;

        public MockCrawlResult(int totalUrlsVisited) {
            super(totalUrlsVisited);
        }

        public MockCrawlResult() {
            super(10);
        }
    }

    @Test
    void givenCrawlResult_whenSaveToPath_thenFileContainsJson() throws IOException {
        MockCrawlResult result = new MockCrawlResult();
        Path outputPath = tempDir.resolve("crawlResult.json");

        CrawlResultSerializer<MockCrawlResult> serializer = new CrawlResultSerializer<>(result);
        serializer.saveToPath(outputPath);

        try (BufferedReader reader = Files.newBufferedReader(outputPath)) {
            ObjectMapper mapper = new ObjectMapper();
            MockCrawlResult readResult = mapper.readValue(reader, MockCrawlResult.class);

            assertNotNull(readResult);
            assertEquals("success", readResult.status);
            assertEquals(10, readResult.pagesCrawled);
            assertEquals(10, readResult.getTotalUrlsVisited());
        }
    }

    @Test
    void givenNullPath_whenSaveToPath_thenThrowsNullPointerException() {
        MockCrawlResult result = new MockCrawlResult();

        CrawlResultSerializer<MockCrawlResult> serializer = new CrawlResultSerializer<>(result);

        assertThrows(NullPointerException.class, () -> serializer.saveToPath(null));
    }

    @Test
    void givenNullWriter_whenSaveToWriter_thenThrowsNullPointerException() {
        MockCrawlResult result = new MockCrawlResult();

        CrawlResultSerializer<MockCrawlResult> serializer = new CrawlResultSerializer<>(result);

        assertThrows(NullPointerException.class, () -> serializer.saveToWriter(null));
    }
}
