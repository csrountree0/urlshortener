package com.csr.urlshortner.dto;

import java.time.LocalDateTime;

public class AnalyticsResponse {

    private final String originalUrl;
    private final String ipAddress;
    private final String clickedAt;
    private final String deviceType;
    private final String browser;
    private final String referrer;
    private final String country;

    public AnalyticsResponse(String originalUrl, String ipAddress, LocalDateTime clickedAt, String deviceType,
                              String browser, String referrer, String country) {
        this.originalUrl = originalUrl;
        this.ipAddress = ipAddress;
        this.clickedAt = clickedAt != null ? clickedAt.toString() : null;
        this.deviceType = deviceType;
        this.browser = browser;
        this.referrer = referrer;
        this.country = country;
    }

    public String getOriginalUrl() { return originalUrl; }
    public String getIpAddress() { return ipAddress; }
    public String getClickedAt() { return clickedAt; }
    public String getDeviceType() { return deviceType; }
    public String getBrowser() { return browser; }
    public String getReferrer() { return referrer; }
    public String getCountry() { return country; }

}
