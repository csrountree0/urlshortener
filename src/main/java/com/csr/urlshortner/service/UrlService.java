package com.csr.urlshortner.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.UrlMappingRepository;

@Service
public class UrlService {

    private final UrlMappingRepository repository;

    public UrlService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public UrlMapping createShortUrl(String originalUrl) {
        
        String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        while(repository.findByShortCode(shortCode).isPresent()){
            shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl(originalUrl);
        return repository.save(mapping);
    }

    @Transactional
    public UrlMapping getOriginalUrl(String shortCode) {
        return repository.findByShortCode(shortCode).orElseThrow(()->new RuntimeException("Shortcode not found: " + shortCode));
    }

}
