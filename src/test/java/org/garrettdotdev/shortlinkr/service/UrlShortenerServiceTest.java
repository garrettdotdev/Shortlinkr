package org.garrettdotdev.shortlinkr.service;

import org.garrettdotdev.shortlinkr.config.ShortlinkProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UrlShortenerServiceTest {

    @Autowired
    private ShortlinkProperties shortlinkProperties;

    private UrlShortenerService urlShortenerService;

    @BeforeEach
    public void setUp() {
        urlShortenerService = new UrlShortenerService(shortlinkProperties);
    }

    @Test
    public void testEncode() {
        String longUrl = "http://example.com";
        String shortUrl = urlShortenerService.encode(longUrl);
        assertFalse(shortUrl.isEmpty());
        assertTrue(shortUrl.startsWith(shortlinkProperties.getBaseUrl() + "/"));
    }

    @Test
    public void testDecode() {
        String longUrl = "http://example.com";
        String shortUrl = urlShortenerService.encode(longUrl);
        String decodedUrl = urlShortenerService.decode(shortUrl);
        assertEquals(longUrl, decodedUrl);
    }

    @Test
    public void testEncodeEmptyUrl() {
        String longUrl = "";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.encode(longUrl);
        });
        assertEquals("Cannot encode empty URL", exception.getMessage());
    }

    @Test
    public void testDecodeEmptyUrl() {
        String shortUrl = "";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.decode(shortUrl);
        });
        assertEquals("Cannot decode empty URL", exception.getMessage());
    }

    @Test
    public void testEncodeInvalidUrl() {
        String longUrl = "invalid-url";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.encode(longUrl);
        });
        assertEquals("Invalid URL", exception.getMessage());
    }

    @Test
    public void testDecodeInvalidUrl() {
        String shortUrl = "http://invalid-url";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.decode(shortUrl);
        });
        assertEquals("Invalid URL", exception.getMessage());
    }

    @Test
    public void testDecodeNonExistentUrl() {
        String shortUrl = shortlinkProperties.getBaseUrl() + "/nonexistent";
        String decodedUrl = urlShortenerService.decode(shortUrl);
        assertEquals("URL not found", decodedUrl);
    }

    @Test
    public void testEncodeTooManyConcurrentRequests() throws InterruptedException, ExecutionException {
        int maxConcurrentRequests = shortlinkProperties.getMaxConcurrentRequests();
        CountDownLatch startLatch = new CountDownLatch(1);

        try (ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentRequests + 1)) {
            List<Future<Void>> futures = new ArrayList<>();


            for (int i = 0; i < maxConcurrentRequests + 1; i++) {
                futures.add(executorService.submit(() -> {
                    urlShortenerService.setDelay(100);
                    try {
                        startLatch.await();
                        urlShortenerService.encode("http://example.com");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }));
            }

            startLatch.countDown();

            boolean exceptionThrown = false;
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof IllegalStateException) {
                        assertEquals("Too many concurrent requests", e.getCause().getMessage());
                        exceptionThrown = true;
                    }
                }
            }

            if (!exceptionThrown) {
                fail("Expected IllegalStateException to be thrown");
            }
        } finally {
            synchronized (UrlShortenerService.class) {
                urlShortenerService.clearDelay();
            }
        }
    }

    @Test
    public void testDecodeTooManyConcurrentRequests() throws InterruptedException, ExecutionException {
        int maxConcurrentRequests = shortlinkProperties.getMaxConcurrentRequests();
        CountDownLatch startLatch = new CountDownLatch(1);

        try (ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentRequests + 1)) {
            List<Future<Void>> futures = new ArrayList<>();


            for (int i = 0; i < maxConcurrentRequests + 1; i++) {
                futures.add(executorService.submit(() -> {
                    urlShortenerService.setDelay(100);
                    try {
                        startLatch.await();
                        urlShortenerService.decode(shortlinkProperties.getBaseUrl() + "/short");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }));
            }

            startLatch.countDown();

            boolean exceptionThrown = false;
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof IllegalStateException) {
                        assertEquals("Too many concurrent requests", e.getCause().getMessage());
                        exceptionThrown = true;
                    }
                }
            }

            if (!exceptionThrown) {
                fail("Expected IllegalStateException to be thrown");
            }
        } finally {
            synchronized (UrlShortenerService.class) {
                urlShortenerService.clearDelay();
            }
        }
    }

}