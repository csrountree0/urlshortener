package com.csr.urlshortner.dto;

import java.time.LocalDateTime;

public class ShortenRequest {

    private String originalUrl;
    private LocalDateTime expiresAt;

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

}
