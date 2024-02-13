package com.webtracer.crawler;

import com.google.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * DomainThrottler is responsible for throttling HTTP requests to specific domains.
 * <p>
 * This class ensures that requests to the same domain are spaced out by a specified
 * delay to prevent overwhelming the server and to avoid hitting rate limits (e.g., HTTP 429 responses).
 * It uses semaphores to manage the throttling, ensuring that only one request per domain
 * is processed at a time, with a delay between each request.
 * </p>
 */
public final class DomainThrottler {

    private final ConcurrentMap<String, Semaphore> domainSemaphores;
    private final long delayBetweenRequests;

    @Inject
    public DomainThrottler(long delayBetweenRequests) {
        this.domainSemaphores = new ConcurrentHashMap<>();
        this.delayBetweenRequests = delayBetweenRequests;
    }

    /**
     * Acquires a permit for the specified domain, ensuring that no more than one
     * request to that domain is processed at a time. After the specified delay,
     * the semaphore is released, allowing the next request to proceed.
     *
     * @param domain The domain for which the request is being throttled.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public void acquire(String domain) throws InterruptedException {
        if(delayBetweenRequests == 0) return;

        // Get or create a semaphore for the specified domain
        Semaphore semaphore = domainSemaphores.computeIfAbsent(domain, d -> new Semaphore(1));

        // Acquire the semaphore to block other requests to the same domain
        semaphore.acquire();

        // Introduce a delay between requests to the same domain
        TimeUnit.MILLISECONDS.sleep(delayBetweenRequests);

        // Release the semaphore to allow the next request to proceed
        semaphore.release();
    }

}
