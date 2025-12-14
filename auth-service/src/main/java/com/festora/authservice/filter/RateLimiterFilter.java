package com.festora.authservice.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimiterFilter implements Filter {
    private final Bucket bucket;

    public RateLimiterFilter(Bucket bucket) {
        this.bucket = bucket;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        int TOKEN_CONSUME_LIMIT = 1;
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(TOKEN_CONSUME_LIMIT);
        if (probe.isConsumed()) {
            chain.doFilter(request, response);
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            long waitForSec = probe.getNanosToWaitForRefill();
            ((HttpServletResponse) response).addHeader("msg", "try after" + waitForSec);
            ((HttpServletResponse) response).setStatus(429);
        }
    }
}
