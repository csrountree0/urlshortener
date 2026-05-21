package com.csr.urlshortner.service;

import com.csr.urlshortner.dto.GlobalAnalyticsResponse;
import com.csr.urlshortner.dto.GlobalAnalyticsResponse.RecentClick;
import com.csr.urlshortner.entity.Analytics;
import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.AnalyticsRepository;
import com.csr.urlshortner.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final GeoLocationService geoLocationService;
    private final SseService sseService;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
                            UrlMappingRepository urlMappingRepository,
                            GeoLocationService geoLocationService,
                            SseService sseService) {
        this.analyticsRepository = analyticsRepository;
        this.urlMappingRepository = urlMappingRepository;
        this.geoLocationService = geoLocationService;
        this.sseService = sseService;
    }

    @Transactional
    public void recordClick(UrlMapping urlMapping, String ipAddress, String userAgent, String referrer) {
        String deviceType = parseDeviceType(userAgent);
        String browser = parseBrowser(userAgent);
        String country = geoLocationService.lookupCountry(ipAddress);

        analyticsRepository.save(new Analytics(
            urlMapping,
            ipAddress,
            deviceType,
            browser,
            referrer != null ? referrer : "direct",
            country
        ));

        sseService.pushClick(new RecentClick(country, deviceType + "/" + browser, java.time.LocalDateTime.now()));
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

    public GlobalAnalyticsResponse getGlobalAnalytics() {
        long totalLinks = urlMappingRepository.count();

        long linksCreatedLastHour = urlMappingRepository.countByCreatedAtAfter(LocalDateTime.now().minusHours(1));

        List<GlobalAnalyticsResponse.RecentClick> recentActivity = analyticsRepository
            .findTop20ByOrderByClickedAtDesc()
            .stream()
            .map(a -> new GlobalAnalyticsResponse.RecentClick(
                a.getCountry(),
                a.getDeviceType() + "/" + a.getBrowser(),
                a.getClickedAt()
            ))
            .collect(Collectors.toList());

        List<Object[]> countryCounts = analyticsRepository.countGroupedByCountry();
        String topCountry = countryCounts.isEmpty() ? "N/A" : (String) countryCounts.get(0)[0];

        long clicksLastMinute = analyticsRepository.countByClickedAtAfter(LocalDateTime.now().minusMinutes(1));

        return new GlobalAnalyticsResponse(totalLinks, linksCreatedLastHour, recentActivity, topCountry, clicksLastMinute);
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
