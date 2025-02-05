package org.garrettdotdev.shortlinkr.service;

import org.garrettdotdev.shortlinkr.config.ShortlinkProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    public void testDecodeNonExistentUrl() {
        String shortUrl = shortlinkProperties.getBaseUrl() + "/nonexistent";
        String decodedUrl = urlShortenerService.decode(shortUrl);
        assertEquals("URL not found", decodedUrl);
    }

    @Test
    public void testDecodeEmptyUrl() {
        String shortUrl = "";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.decode(shortUrl);
        });
        assertEquals("Cannot decode empty URL", exception.getMessage());
    }
}