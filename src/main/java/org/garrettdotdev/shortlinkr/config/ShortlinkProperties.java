package org.garrettdotdev.shortlinkr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "shortlink")
@Getter
@Setter
public class ShortlinkProperties {
    private String baseUrl;
    private int maxConcurrentRequests;
}
