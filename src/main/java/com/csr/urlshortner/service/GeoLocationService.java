package com.csr.urlshortner.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
public class GeoLocationService {

    private DatabaseReader geoReader;

    @PostConstruct
    public void init() throws IOException {
        geoReader = new DatabaseReader.Builder(
            new ClassPathResource("GeoLite2-Country.mmdb").getInputStream()
        ).build();
    }

    public String lookupCountry(String ipAddress) {
        if (ipAddress == null) return "unknown";
        try {
            InetAddress addr = InetAddress.getByName(ipAddress.split(",")[0].trim());
            return geoReader.country(addr).getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            return "unknown";
        }
    }

}
