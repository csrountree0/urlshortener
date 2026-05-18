package com.csr.urlshortner.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.UrlMappingRepository;

@Service
public class UrlService {

    private static final int DEFAULT_EXPIRY_DAYS = 7;
    private static final int MAX_EXPIRY_DAYS = 31;

    private final UrlMappingRepository repository;

    public UrlService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public UrlMapping createShortUrl(String originalUrl, LocalDateTime expiresAt) {
        String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        while(repository.findByShortCode(shortCode).isPresent()){
            shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        String analyticsToken = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl(originalUrl);
        mapping.setAnalyticsToken(analyticsToken);
        LocalDateTime maxExpiry = LocalDateTime.now().plusDays(MAX_EXPIRY_DAYS);
        LocalDateTime resolvedExpiry = expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS);
        mapping.setExpiresAt(resolvedExpiry.isAfter(maxExpiry) ? maxExpiry : resolvedExpiry);
        return repository.save(mapping);
    }

    @Transactional
    public UrlMapping getOriginalUrl(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode).orElseThrow(
            () -> new RuntimeException("Short code not found: " + shortCode)
        );

        if (mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Short code has expired: " + shortCode);
        }

        return mapping;
    }

}
