package com.csr.urlshortner.scheduler;

import com.csr.urlshortner.entity.UrlMapping;
import com.csr.urlshortner.repository.AnalyticsRepository;
import com.csr.urlshortner.repository.UrlMappingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CleanupScheduler {

    private final UrlMappingRepository urlMappingRepository;
    private final AnalyticsRepository analyticsRepository;

    public CleanupScheduler(UrlMappingRepository urlMappingRepository, AnalyticsRepository analyticsRepository) {
        this.urlMappingRepository = urlMappingRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // runs every hour
    @Transactional
    public void deleteExpiredLinks() {
        List<UrlMapping> expired = urlMappingRepository.findByExpiresAtBefore(LocalDateTime.now());
        for (UrlMapping mapping : expired) {
            analyticsRepository.deleteByUrlMapping(mapping);
            urlMappingRepository.delete(mapping);
        }
    }

}
