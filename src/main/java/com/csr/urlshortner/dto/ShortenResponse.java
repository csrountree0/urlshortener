package com.csr.urlshortner.dto;

import java.time.LocalDateTime;

public class ShortenResponse {

    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private String analyticsToken;
    private LocalDateTime createdAt;

    public ShortenResponse(){}
    public ShortenResponse(String oUrl, String sCode, String sUrl, String aToken, LocalDateTime cAt){
        originalUrl = oUrl;
        shortCode = sCode;
        shortUrl = sUrl;
        analyticsToken = aToken;
        createdAt = cAt;
    }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public String getAnalyticsToken() { return analyticsToken; }
    public void setAnalyticsToken(String analyticsToken) { this.analyticsToken = analyticsToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
