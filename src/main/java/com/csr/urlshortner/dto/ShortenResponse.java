package com.csr.urlshortner.dto;

import java.time.LocalDateTime;

public class ShortenResponse {

    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private LocalDateTime createdAt;

    public ShortenResponse(){}
    public ShortenResponse(String oUrl, String sCode, String sUrl, LocalDateTime cAt){
        originalUrl = oUrl;
        shortCode = sCode;
        shortUrl = sUrl;
        createdAt = cAt;
    }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
