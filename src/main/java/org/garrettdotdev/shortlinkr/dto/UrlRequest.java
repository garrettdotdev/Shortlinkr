package org.garrettdotdev.shortlinkr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request containing a URL
 */
@Getter
@Setter
public class UrlRequest {
    @NotBlank
    private String url;
}
