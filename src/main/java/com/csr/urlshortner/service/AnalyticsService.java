package com.csr.urlshortner.service;

import com.csr.urlshortner.entity.Analytics;
import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.AnalyticsRepository;
import com.csr.urlshortner.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final UrlMappingRepository urlMappingRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository, UrlMappingRepository urlMappingRepository) {
        this.analyticsRepository = analyticsRepository;
        this.urlMappingRepository = urlMappingRepository;
    }

    @Transactional
    public void recordClick(UrlMapping urlMapping, String ipAddress, String userAgent, String referrer) {
        analyticsRepository.save(new Analytics(
            urlMapping,
            ipAddress,
            parseDeviceType(userAgent),
            parseBrowser(userAgent),
            referrer != null ? referrer : "direct"
        ));
    }

    public List<Analytics> getClicksForToken(String analyticsToken) {
        UrlMapping mapping = urlMappingRepository.findByAnalyticsToken(analyticsToken)
            .orElseThrow(() -> new RuntimeException("Analytics token not found: " + analyticsToken));
        return analyticsRepository.findByUrlMapping(mapping);
    }

    public long getTotalClicksForToken(String analyticsToken) {
        UrlMapping mapping = urlMappingRepository.findByAnalyticsToken(analyticsToken)
            .orElseThrow(() -> new RuntimeException("Analytics token not found: " + analyticsToken));
        return analyticsRepository.countByUrlMapping(mapping);
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "mobile";
        if (ua.contains("tablet") || ua.contains("ipad")) return "tablet";
        return "desktop";
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari")) return "Safari";
        if (ua.contains("opera")) return "Opera";
        return "other";
    }

}
