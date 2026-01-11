package com.haorenserverwebsite.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全相关配置
 * 包含HTTP安全头、速率限制、输入过滤等
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        // 安全头配置
        http.headers(h -> h
            .frameOptions(f -> f.sameOrigin())
            .contentTypeOptions(c -> {})
            .xssProtection(x -> x.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            .contentSecurityPolicy(csp -> csp.policyDirectives(cspRules()))
            .permissionsPolicy(p -> p.policy("geolocation=(), camera=(), microphone=()"))
        );
        
        super.configure(http);
    }

    /**
     * CSP策略，限制资源加载来源
     */
    private String cspRules() {
        return String.join("; ",
            "default-src 'self'",
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' https://fonts.gstatic.com data:",
            "img-src 'self' data: blob:",
            "connect-src 'self' ws: wss:",
            "frame-ancestors 'self'",
            "form-action 'self'",
            "base-uri 'self'",
            "object-src 'none'"
        );
    }

    /**
     * 请求频率限制过滤器
     * 防止恶意刷接口或简单的DDoS
     */
    @Component
    @Order(1)
    public static class RateLimiter extends OncePerRequestFilter {

        // IP -> 令牌桶
        private final Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();
        
        // 被封禁的IP及封禁时间
        private final Map<String, Long> blocked = new ConcurrentHashMap<>();
        
        // 每分钟允许100次请求
        private static final int RATE = 100;
        
        // 封禁时长1小时
        private static final long BAN_MS = 3600_000L;

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
                throws ServletException, IOException {
            
            String ip = extractIp(req);
            
            // 检查是否被封
            if (isBlocked(ip)) {
                resp.setStatus(403);
                resp.getWriter().write("Access denied");
                return;
            }
            
            Bucket bucket = bucketMap.computeIfAbsent(ip, k -> createBucket());
            
            if (bucket.tryConsume(1)) {
                addSecHeaders(resp);
                chain.doFilter(req, resp);
            } else {
                // 超限则封禁
                blocked.put(ip, System.currentTimeMillis());
                resp.setStatus(429);
                resp.setHeader("Retry-After", "60");
                resp.getWriter().write("Too many requests");
            }
        }

        private Bucket createBucket() {
            return Bucket.builder()
                .addLimit(Bandwidth.classic(RATE, Refill.greedy(RATE, Duration.ofMinutes(1))))
                .build();
        }

        private String extractIp(HttpServletRequest req) {
            // 依次检查代理头
            String[] hdrs = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"};
            for (String h : hdrs) {
                String v = req.getHeader(h);
                if (v != null && !v.isEmpty() && !"unknown".equalsIgnoreCase(v)) {
                    int idx = v.indexOf(',');
                    return idx > 0 ? v.substring(0, idx).trim() : v.trim();
                }
            }
            return req.getRemoteAddr();
        }

        private boolean isBlocked(String ip) {
            Long t = blocked.get(ip);
            if (t == null) return false;
            if (System.currentTimeMillis() - t < BAN_MS) {
                return true;
            }
            blocked.remove(ip);
            return false;
        }

        private void addSecHeaders(HttpServletResponse resp) {
            resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            resp.setHeader("Cache-Control", "no-store");
        }
    }

    /**
     * 输入安全过滤器
     * 简单检测常见的XSS和注入模式
     */
    @Component
    @Order(2)
    public static class InputFilter extends OncePerRequestFilter {

        private static final String[] BAD_PATTERNS = {
            "<script", "javascript:", "onload=", "onerror=",
            "<iframe", "eval(", "document.cookie",
            "SELECT ", "INSERT ", "DELETE ", "DROP ", "UNION ", "OR 1=1"
        };

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
                throws ServletException, IOException {
            
            for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
                for (String val : e.getValue()) {
                    if (hasBadPattern(val)) {
                        resp.setStatus(400);
                        resp.getWriter().write("Invalid input");
                        return;
                    }
                }
            }
            chain.doFilter(req, resp);
        }

        private boolean hasBadPattern(String s) {
            if (s == null) return false;
            String lower = s.toLowerCase();
            for (String p : BAD_PATTERNS) {
                if (lower.contains(p.toLowerCase())) return true;
            }
            return false;
        }
    }
}
