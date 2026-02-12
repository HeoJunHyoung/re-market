package com.example.remarket.global.monitoring;

public class RequestContextHolder { // ThreadLocal 관리

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    public static void initContext(RequestContext context) {
        CONTEXT.remove(); // 기존 데이터 정리를 위해 remove 선행
        CONTEXT.set(context);
    }

    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}