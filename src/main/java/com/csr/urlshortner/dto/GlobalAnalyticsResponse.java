package com.csr.urlshortner.dto;

import java.time.LocalDateTime;
import java.util.List;


public class GlobalAnalyticsResponse {

    private final long totalLinksGenerated;
    private final long linksCreatedLastHour;
    private final List<RecentClick> recentActivity;
    private final String topCountry;
    private final long clicksLastMinute;

    public GlobalAnalyticsResponse(long totalLinksGenerated, long linksCreatedLastHour,
                                   List<RecentClick> recentActivity, String topCountry,
                                   long clicksLastMinute) {
        this.totalLinksGenerated = totalLinksGenerated;
        this.linksCreatedLastHour = linksCreatedLastHour;
        this.recentActivity = recentActivity;
        this.topCountry = topCountry;
        this.clicksLastMinute = clicksLastMinute;
    }

    public long getTotalLinksGenerated() { return totalLinksGenerated; }
    public long getLinksCreatedLastHour() { return linksCreatedLastHour; }
    public List<RecentClick> getRecentActivity() { return recentActivity; }
    public String getTopCountry() { return topCountry; }
    public long getClicksLastMinute() { return clicksLastMinute; }

    public static class RecentClick {
        private final String country;
        private final String origin;
        private final LocalDateTime clickedAt;

        public RecentClick(String country, String origin, LocalDateTime clickedAt) {
            this.country = country;
            this.origin = origin;
            this.clickedAt = clickedAt;
        }

        public String getCountry() { return country; }
        public String getOrigin() { return origin; }
        public LocalDateTime getClickedAt() { return clickedAt; }
    }
}
