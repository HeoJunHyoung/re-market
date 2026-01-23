package com.example.secondhandmarket.global.monitoring;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCountInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;
    private static final String UNKNOWN_PATH = "UNKNOWN_PATH";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String httpMethod = request.getMethod();
        // /items/{itemId} 처럼 패턴화된 URL을 가져옴 (그냥 URI를 쓰면 metric이 폭발함)
        String bestMatchPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        RequestContext ctx = RequestContext.of(httpMethod, bestMatchPath != null ? bestMatchPath : UNKNOWN_PATH);
        RequestContextHolder.initContext(ctx);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext ctx = RequestContextHolder.getContext();
        if (ctx != null) {
            // 로그로 찍어서 바로 확인해볼 수도 있음
             log.info("[QUERY MONITOR] Path: {} | Counts: {}", ctx.getBestMatchPath(), ctx.getQueryCountByType());

            ctx.getQueryCountByType().forEach((type, count) -> recordMetrics(ctx, type, count));
        }
        RequestContextHolder.clear();
    }

    private void recordMetrics(RequestContext ctx, QueryType queryType, Integer count) {
        DistributionSummary.builder("app.query.per_request")
                .tag("path", ctx.getBestMatchPath())
                .tag("http_method", ctx.getHttpMethod())
                .tag("query_type", queryType.name())
                .description("SQL query count per HTTP request")
                .register(meterRegistry)
                .record(count);
    }
}