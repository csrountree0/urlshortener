package com.csr.urlshortner.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.csr.urlshortner.dto.AnalyticsResponse;
import com.csr.urlshortner.dto.GlobalAnalyticsResponse;
import com.csr.urlshortner.dto.ShortenRequest;
import com.csr.urlshortner.dto.ShortenResponse;
import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.service.AnalyticsService;
import com.csr.urlshortner.service.UrlService;

@RestController
public class UrlController {

    @Value("${app.base-url}")
    private String baseUrl;
    private final UrlService urlService;
    private final AnalyticsService analyticsService;

    public UrlController(UrlService urlService, AnalyticsService analyticsService) {
        this.urlService = urlService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<ShortenResponse> createShortUrl(@RequestBody ShortenRequest request) {
        UrlMapping mapping = urlService.createShortUrl(request.getOriginalUrl(), request.getExpiresAt());

        ShortenResponse response = new ShortenResponse(
            mapping.getOriginalUrl(),
            mapping.getShortCode(),
            baseUrl + "/" + mapping.getShortCode(),
            baseUrl + "/analytics/" + mapping.getAnalyticsToken(),
            mapping.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Referer", required = false) String referrer) {

        if (shortCode.equals("favicon.ico")) return ResponseEntity.notFound().build();

        UrlMapping mapping = urlService.getOriginalUrl(shortCode);
        analyticsService.recordClick(mapping, forwardedIp, userAgent, referrer);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(mapping.getOriginalUrl()))
            .build();
    }

    @GetMapping("/analytics/global")
    public ResponseEntity<GlobalAnalyticsResponse> getGlobalAnalytics() {
        return ResponseEntity.ok(analyticsService.getGlobalAnalytics());
    }

    @GetMapping("/analytics/{token}")
    public ResponseEntity<List<AnalyticsResponse>> getAnalytics(@PathVariable String token) {
        return ResponseEntity.ok(
            analyticsService.getClicksForToken(token).stream()
                .map(a -> new AnalyticsResponse(
                    a.getUrlMapping().getOriginalUrl(),
                    a.getIpAddress(),
                    a.getClickedAt(),
                    a.getDeviceType(),
                    a.getBrowser(),
                    a.getReferrer(),
                    a.getCountry()
                ))
                .collect(Collectors.toList())
        );
    }

}
