package com.csr.urlshortner.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.exception.ShortCodeExpiredException;
import com.csr.urlshortner.exception.ShortCodeNotFoundException;
import com.csr.urlshortner.repository.UrlMappingRepository;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private UrlService urlService;

    private UrlMapping savedMapping;

    @BeforeEach
    void setUp() {
        savedMapping = new UrlMapping();
        savedMapping.setOriginalUrl("https://example.com");
        savedMapping.setShortCode("abc12345");
        savedMapping.setExpiresAt(LocalDateTime.now().plusDays(7));
    }

    // Happy path: no collision, service saves the mapping and returns it
    @Test
    void createShortUrl_savesAndReturnsMapping() {
        when(repository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(UrlMapping.class))).thenReturn(savedMapping);

        UrlMapping result = urlService.createShortUrl("https://example.com", null);

        assertEquals("https://example.com", result.getOriginalUrl());
        assertNotNull(result.getShortCode());
        verify(repository, times(1)).save(any(UrlMapping.class));
    }

    // Collision: first short code already exists, service generates a new one and retries
    @Test
    void createShortUrl_retriesOnCollision() {
        UrlMapping existing = new UrlMapping();
        existing.setShortCode("collision");

        when(repository.findByShortCode(anyString()))
            .thenReturn(Optional.of(existing))
            .thenReturn(Optional.empty());
        when(repository.save(any(UrlMapping.class))).thenReturn(savedMapping);

        UrlMapping result = urlService.createShortUrl("https://example.com", null);

        assertNotNull(result);
        verify(repository, times(2)).findByShortCode(anyString());
    }

    // Found: short code exists in the database, service returns the mapping
    @Test
    void getOriginalUrl_returnsMapping_whenFound() {
        when(repository.findByShortCode("abc12345")).thenReturn(Optional.of(savedMapping));

        UrlMapping result = urlService.getOriginalUrl("abc12345");

        assertEquals("https://example.com", result.getOriginalUrl());
    }

    // Not found: short code doesn't exist, service throws ShortCodeNotFoundException
    @Test
    void getOriginalUrl_throwsException_whenNotFound() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        ShortCodeNotFoundException ex = assertThrows(ShortCodeNotFoundException.class, () -> urlService.getOriginalUrl("missing"));
        assertTrue(ex.getMessage().contains("missing"));
    }

    // Expired: short code exists but past its expiry date, service throws ShortCodeExpiredException
    @Test
    void getOriginalUrl_throwsException_whenExpired() {
        savedMapping.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(repository.findByShortCode("abc12345")).thenReturn(Optional.of(savedMapping));

        ShortCodeExpiredException ex = assertThrows(ShortCodeExpiredException.class, () -> urlService.getOriginalUrl("abc12345"));
        assertTrue(ex.getMessage().contains("expired"));
    }

}
