package org.garrettdotdev.shortlinkr.controller;

import org.garrettdotdev.shortlinkr.dto.UrlRequest;
import org.garrettdotdev.shortlinkr.service.UrlShortenerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import jakarta.validation.Valid;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    /**
     * Encodes a long URL into a short URL
     *
     * @param request the request containing the long URL
     * @return the short URL
     */
    @PostMapping("/encode")
    public ResponseEntity<Map<String, String>> encode(@Valid @RequestBody UrlRequest request) {
        String longUrl = request.getUrl();
        String shortUrl = urlShortenerService.encode(longUrl);
        return ResponseEntity.ok(Map.of("url", shortUrl));
    }

    /**
     * Decodes a short URL into a long URL
     *
     * @param request the request containing the short URL
     * @return the long URL
     */
    @PostMapping("/decode")
    public ResponseEntity<Map<String, String>> decode(@Valid @RequestBody UrlRequest request) {
        String shortUrl = request.getUrl();
        String longUrl = urlShortenerService.decode(shortUrl);
        return ResponseEntity.ok(Map.of("url", longUrl));
    }
}
