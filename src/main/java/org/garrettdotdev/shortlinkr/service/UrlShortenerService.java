package org.garrettdotdev.shortlinkr.service;

import org.garrettdotdev.shortlinkr.config.ShortlinkProperties;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class UrlShortenerService {

    private final ConcurrentHashMap<String, String> urlMap = new ConcurrentHashMap<>();
    private final String baseUrl;
    private final Semaphore semaphore;

    public UrlShortenerService(ShortlinkProperties shortlinkProperties) {
        this.baseUrl = shortlinkProperties.getBaseUrl();
        this.semaphore = new Semaphore(shortlinkProperties.getMaxConcurrentRequests());
    }

    /**
     * Encodes a long URL into a short URL
     *
     * @param longUrl the long URL to encode
     * @return the short URL
     */
    public String encode(String longUrl) {
        if(!semaphore.tryAcquire()) {
            throw new IllegalStateException("Too many concurrent requests");
        }
        try {
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
        } finally {
            semaphore.release();
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
            throw new IllegalStateException("Too many concurrent requests");
        }
        try {
            if (shortUrl == null || shortUrl.isBlank()) {
                throw new IllegalArgumentException("Cannot decode empty URL");
            }
            if (!shortUrl.startsWith(baseUrl)) {
                throw new IllegalArgumentException("Invalid URL");
            }
            String shortCode = shortUrl.replace(baseUrl + "/", "");
            return urlMap.getOrDefault(shortCode, "URL not found");
        } finally {
            semaphore.release();
        }
    }

}
