package org.garrettdotdev.shortlinkr.service;

import org.garrettdotdev.shortlinkr.config.ShortlinkProperties;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UrlShortenerService {

    private final ConcurrentHashMap<String, String> urlMap;
    private final String baseUrl;
    private final ThreadLocal<Long> delay = ThreadLocal.withInitial(() -> 0L);
    private final Semaphore semaphore;

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class);

    public UrlShortenerService(ShortlinkProperties shortlinkProperties) {
        this.urlMap = new ConcurrentHashMap<>();
        this.baseUrl = shortlinkProperties.getBaseUrl();
        this.semaphore = new Semaphore(shortlinkProperties.getMaxConcurrentRequests());
    }

    public void setDelay(long millis) {
        delay.set(millis);
    }

    public void clearDelay() {
        delay.remove();
    }

    /**
     * Encodes a long URL into a short URL
     *
     * @param longUrl the long URL to encode
     * @return the short URL
     */
    public String encode(String longUrl) {
        if(!semaphore.tryAcquire()) {
            logger.debug("Semaphore permits: {}", semaphore.availablePermits());
            throw new IllegalStateException("Too many concurrent requests");
        }
        try {
            if(this.delay.get() > 0) {
                logger.debug("Encode delaying for {} ms", this.delay.get());
                Thread.sleep(this.delay.get());
            }
            if (longUrl == null || longUrl.isBlank()) {
                throw new IllegalArgumentException("Cannot encode empty URL");
            }
            try {
                URI uri = new URI(longUrl);
                if(uri.getScheme() == null || uri.getHost() == null) {
                    throw new IllegalArgumentException("Invalid URL");
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URL");
            }
            String shortCode = Base64.getUrlEncoder().encodeToString(longUrl.getBytes()).substring(0, 6);
            urlMap.put(shortCode, longUrl);
            return baseUrl + "/" + shortCode;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
            logger.debug("Released semaphore permit. Available permits: {}", semaphore.availablePermits());
        }
    }

    /**
     * Decodes a short URL into a long URL
     *
     * @param shortUrl the short URL to decode
     * @return the long URL
     */
    public String decode(String shortUrl) {
        if(!semaphore.tryAcquire()) {
            logger.debug("Semaphore permits: {}", semaphore.availablePermits());
            throw new IllegalStateException("Too many concurrent requests");
        }
        try {
            if(this.delay.get() > 0) {
                logger.debug("Decode delaying for {} ms", this.delay.get());
                Thread.sleep(this.delay.get());
            }
            if (shortUrl == null || shortUrl.isBlank()) {
                throw new IllegalArgumentException("Cannot decode empty URL");
            }
            if (!shortUrl.startsWith(baseUrl)) {
                throw new IllegalArgumentException("Invalid URL");
            }
            String shortCode = shortUrl.replace(baseUrl + "/", "");
            return urlMap.getOrDefault(shortCode, "URL not found");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
            logger.debug("Released semaphore permit. Available permits: {}", semaphore.availablePermits());
        }
    }

}
