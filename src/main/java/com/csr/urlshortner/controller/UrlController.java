package com.csr.urlshortner.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.csr.urlshortner.dto.ShortenRequest;
import com.csr.urlshortner.dto.ShortenResponse;
import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.service.UrlService;

@RestController
public class UrlController {

    @Value("${app.base-url}")
    private String baseUrl;
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<ShortenResponse> createShortUrl(@RequestBody ShortenRequest request) {
        UrlMapping mapping = urlService.createShortUrl(request.getOriginalUrl(), request.getExpiresAt());
  
        ShortenResponse response = new ShortenResponse(
            mapping.getOriginalUrl(),
            mapping.getShortCode(),
            baseUrl + "/" + mapping.getShortCode(),
            mapping.getAnalyticsToken(),
            mapping.getCreatedAt()
        );

       return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        UrlMapping mapping = urlService.getOriginalUrl(shortCode);
        
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(mapping.getOriginalUrl()))
            .build();
    }

}
