package com.csr.urlshortner.repository;

import com.csr.urlshortner.entity.Analytics;
import com.csr.urlshortner.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {

    List<Analytics> findByUrlMapping(UrlMapping urlMapping);

    long countByUrlMapping(UrlMapping urlMapping);

    void deleteByUrlMapping(UrlMapping urlMapping);

    List<Analytics> findTop20ByOrderByClickedAtDesc();

    long countByClickedAtAfter(LocalDateTime since);

    @Query("SELECT a.country, COUNT(a) FROM Analytics a GROUP BY a.country ORDER BY COUNT(a) DESC")
    List<Object[]> countGroupedByCountry();

}
