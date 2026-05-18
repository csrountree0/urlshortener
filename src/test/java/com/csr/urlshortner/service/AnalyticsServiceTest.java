package com.csr.urlshortner.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.csr.urlshortner.entity.Analytics;
import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.AnalyticsRepository;
import com.csr.urlshortner.repository.UrlMappingRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private GeoLocationService geoLocationService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private UrlMapping urlMapping;

    @BeforeEach
    void setUp() {
        urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc12345");
        urlMapping.setOriginalUrl("https://example.com");
        urlMapping.setAnalyticsToken("token123");
        lenient().when(geoLocationService.lookupCountry(any())).thenReturn("United States");
    }

    // Records a click with correct device type and browser parsed from User-Agent
    @Test
    void recordClick_savesAnalyticsWithParsedUserAgent() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/120.0";

        analyticsService.recordClick(urlMapping, "192.168.1.1", userAgent, "https://media.com");

        ArgumentCaptor<Analytics> captor = ArgumentCaptor.forClass(Analytics.class);
        verify(analyticsRepository).save(captor.capture());

        Analytics saved = captor.getValue();
        assertEquals("192.168.1.1", saved.getIpAddress());
        assertEquals("desktop", saved.getDeviceType());
        assertEquals("Chrome", saved.getBrowser());
        assertEquals("https://media.com", saved.getReferrer());
    }

    // Null referrer defaults to "direct" instead of null
    @Test
    void recordClick_defaultsReferrerToDirect_whenNull() {
        analyticsService.recordClick(urlMapping, "192.168.1.1", "Mozilla/5.0", null);

        ArgumentCaptor<Analytics> captor = ArgumentCaptor.forClass(Analytics.class);
        verify(analyticsRepository).save(captor.capture());

        assertEquals("direct", captor.getValue().getReferrer());
    }

    // Mobile User-Agent is correctly identified as mobile device type
    @Test
    void recordClick_detectsMobileDevice() {
        String mobileAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) AppleWebKit/605.1.15";

        analyticsService.recordClick(urlMapping, "10.0.0.1", mobileAgent, null);

        ArgumentCaptor<Analytics> captor = ArgumentCaptor.forClass(Analytics.class);
        verify(analyticsRepository).save(captor.capture());

        assertEquals("mobile", captor.getValue().getDeviceType());
    }

    // Valid token returns all click records for that link
    @Test
    void getClicksForToken_returnsClicks_whenTokenFound() {
        Analytics click = new Analytics(urlMapping, "1.2.3.4", "desktop", "Chrome", "direct", "United States");
        when(urlMappingRepository.findByAnalyticsToken("token123")).thenReturn(Optional.of(urlMapping));
        when(analyticsRepository.findByUrlMapping(urlMapping)).thenReturn(List.of(click));

        List<Analytics> result = analyticsService.getClicksForToken("token123");

        assertEquals(1, result.size());
        assertEquals("Chrome", result.get(0).getBrowser());
    }

    // Invalid token throws RuntimeException
    @Test
    void getClicksForToken_throwsException_whenTokenNotFound() {
        when(urlMappingRepository.findByAnalyticsToken("badtoken")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> analyticsService.getClicksForToken("badtoken"));
        assertTrue(ex.getMessage().contains("badtoken"));
    }

    // Returns accurate total click count for a valid token
    @Test
    void getTotalClicksForToken_returnsCount() {
        when(urlMappingRepository.findByAnalyticsToken("token123")).thenReturn(Optional.of(urlMapping));
        when(analyticsRepository.countByUrlMapping(urlMapping)).thenReturn(5L);

        long count = analyticsService.getTotalClicksForToken("token123");

        assertEquals(5L, count);
    }

}
