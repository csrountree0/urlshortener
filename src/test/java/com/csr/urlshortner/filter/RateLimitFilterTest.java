package com.csr.urlshortner.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        chain = mock(FilterChain.class);
    }

    // Redirect requests within limit pass through to the filter chain
    @Test
    void redirect_allowsRequestsWithinLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = buildRequest("GET", "/abc12345", "1.2.3.4");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, chain);
            assertEquals(200, response.getStatus());
        }
        verify(chain, times(10)).doFilter(any(), any());
    }

    // 11th redirect request from same IP gets 429
    @Test
    void redirect_blocksRequestsOverLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(buildRequest("GET", "/abc12345", "1.2.3.4"),
                new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(buildRequest("GET", "/abc12345", "1.2.3.4"), response, chain);

        assertEquals(429, response.getStatus());
        verify(chain, times(10)).doFilter(any(), any());
    }

    // Create requests within limit pass through to the filter chain
    @Test
    void create_allowsRequestsWithinLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = buildRequest("POST", "/api/urls", "1.2.3.4");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, chain);
            assertEquals(200, response.getStatus());
        }
        verify(chain, times(5)).doFilter(any(), any());
    }

    // 6th create request from same IP gets 429
    @Test
    void create_blocksRequestsOverLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(buildRequest("POST", "/api/urls", "1.2.3.4"),
                new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(buildRequest("POST", "/api/urls", "1.2.3.4"), response, chain);

        assertEquals(429, response.getStatus());
        verify(chain, times(5)).doFilter(any(), any());
    }

    // Verify one hitting the limit doesn't affect another
    @Test
    void differentIps_haveIndependentBuckets() throws Exception {
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(buildRequest("GET", "/abc12345", "1.2.3.4"),
                new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(buildRequest("GET", "/abc12345", "5.6.7.8"), response, chain);

        assertEquals(200, response.getStatus());
    }

    // Redirect limit and create limit are independent per IP
    @Test
    void redirectAndCreate_haveIndependentLimits() throws Exception {
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(buildRequest("GET", "/abc12345", "1.2.3.4"),
                new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(buildRequest("POST", "/api/urls", "1.2.3.4"), response, chain);

        assertEquals(200, response.getStatus());
    }

    private MockHttpServletRequest buildRequest(String method, String path, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(path);
        request.addHeader("X-Forwarded-For", ip);
        return request;
    }

}
