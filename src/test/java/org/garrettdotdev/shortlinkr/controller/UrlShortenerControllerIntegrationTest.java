package org.garrettdotdev.shortlinkr.controller;

import org.garrettdotdev.shortlinkr.config.ShortlinkProperties;
import org.garrettdotdev.shortlinkr.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
@AutoConfigureMockMvc
public class UrlShortenerControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private ShortlinkProperties shortlinkProperties;

    @Test
    public void testEncode() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("url", "http://example.com");

        mockMvc.perform(post("/encode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"http://example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", not(emptyString())))
                .andExpect(jsonPath("$.url", startsWith(shortlinkProperties.getBaseUrl() + "/")));
    }

    @Test
    public void testEncodeInvalidUrl() throws Exception {
        mockMvc.perform(post("/encode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"invalid-url\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDecode() throws Exception {
        String longUrl = "http://example.com";
        String shortUrl = urlShortenerService.encode(longUrl);

        mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + shortUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", is(longUrl)));
    }

    @Test
    public void testDecodeNonExistentUrl() throws Exception {
        String shortUrl = shortlinkProperties.getBaseUrl() + "/nonexistent";

        mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + shortUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", is("URL not found")));
    }

    @Test
    public void testDecodeEmptyUrl() throws Exception {
        mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testEncodeEmptyUrl() throws Exception {
        mockMvc.perform(post("/encode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testEncodeNullUrl() throws Exception {
        mockMvc.perform(post("/encode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDecodeNullUrl() throws Exception {
        mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testConcurrencyControlWithinLimit() throws Exception {
        int maxConcurrentRequests = shortlinkProperties.getMaxConcurrentRequests();

        try (ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentRequests)) {
            IntStream.range(0, maxConcurrentRequests).forEach(i ->
                    executor.submit(() -> {
                        try {
                            mockMvc.perform(post("/encode")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content("{\"url\":\"http://example.com/" + i + "\"}"))
                                    .andExpect(status().isOk());
                        } catch (Exception e) {
                            logger.error("Error performing request: ", e);
                        }
                    })
            );

            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Executor service did not terminate within the timeout period.");
            }
        }
    }

    @Test
    public void testConcurrencyControlExceedsLimit() throws Exception {
        int maxConcurrentRequests = shortlinkProperties.getMaxConcurrentRequests();
        int extraRequests = 5;  // Simulate requests exceeding the limit

        try (ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentRequests + extraRequests)) {
            IntStream.range(0, maxConcurrentRequests + extraRequests).forEach(i ->
                    executor.submit(() -> {
                        try {
                            mockMvc.perform(post("/encode")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content("{\"url\":\"http://example.com/" + i + "\"}"))
                                    .andExpect(i >= maxConcurrentRequests ? status().isTooManyRequests() : status().isOk());
                        } catch (Exception e) {
                            logger.error("Error performing request: ", e);
                        }
                    })
            );

            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Executor service did not terminate within the timeout period.");
            }
        }
    }
}