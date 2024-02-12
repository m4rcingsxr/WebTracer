package com.webtracer.crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertTimeout;


import java.time.Duration;

class DomainThrottlerTest {

    private DomainThrottler domainThrottler;

    @BeforeEach
    void setUp() {
        domainThrottler = new DomainThrottler(100);
    }

    @RepeatedTest(5)
    void givenSameDomain_whenAcquiring_thenShouldThrottleRequests() {
        String domain = "example.com";

        assertTimeout(Duration.ofMillis(250), () -> {
            domainThrottler.acquire(domain);
            domainThrottler.acquire(domain);
        });
    }

    @RepeatedTest(5)
    void givenDifferentDomains_whenAcquiring_thenShouldNotThrottleIndependently() throws InterruptedException {
        String domain1 = "example.com";
        String domain2 = "example.org";

        domainThrottler.acquire(domain1);

        assertTimeout(Duration.ofMillis(150), () -> {
            domainThrottler.acquire(domain2);
        });
    }

    @RepeatedTest(5)
    void givenMultipleRequestsToSameDomain_whenAcquiring_thenShouldThrottleEachRequest() {
        String domain = "example.com";

        assertTimeout(Duration.ofMillis(450), () -> {
            domainThrottler.acquire(domain);
            domainThrottler.acquire(domain);
            domainThrottler.acquire(domain);
        });
    }

}
