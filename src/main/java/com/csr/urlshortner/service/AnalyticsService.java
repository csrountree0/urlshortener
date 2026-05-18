package com.csr.urlshortner.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.csr.urlshortner.entity.Analytics;
import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.AnalyticsRepository;
import com.csr.urlshortner.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final UrlMappingRepository urlMappingRepository;
    private DatabaseReader geoReader;

    public AnalyticsService(AnalyticsRepository analyticsRepository, UrlMappingRepository urlMappingRepository) {
        this.analyticsRepository = analyticsRepository;
        this.urlMappingRepository = urlMappingRepository;
    }

    @PostConstruct
    public void initGeoReader() throws IOException {
        geoReader = new DatabaseReader.Builder(
            new ClassPathResource("GeoLite2-Country.mmdb").getInputStream()
        ).build();
    }

    @Transactional
    public void recordClick(UrlMapping urlMapping, String ipAddress, String userAgent, String referrer) {
        analyticsRepository.save(new Analytics(
            urlMapping,
            ipAddress,
            parseDeviceType(userAgent),
            parseBrowser(userAgent),
            referrer != null ? referrer : "direct",
            lookupCountry(ipAddress)
        ));
    }

    private String lookupCountry(String ipAddress) {
        if (ipAddress == null) return "unknown";
        try {
            InetAddress addr = InetAddress.getByName(ipAddress.split(",")[0].trim());
            return geoReader.country(addr).getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            return "unknown";
        }
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
