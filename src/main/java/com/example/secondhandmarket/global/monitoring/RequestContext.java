package com.example.secondhandmarket.global.monitoring;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RequestContext { // 요청별 쿼리 카운트 저장소

    private final String httpMethod;
    private final String bestMatchPath;
    private final Map<QueryType, Integer> queryCountByType = new HashMap<>();

    public RequestContext(String httpMethod, String bestMatchPath) {
        this.httpMethod = httpMethod;
        this.bestMatchPath = bestMatchPath;
    }

    public static RequestContext of(String httpMethod, String bestMatchPath) {
        return new RequestContext(httpMethod, bestMatchPath);
    }

    public void incrementQueryCount(String sql) {
        QueryType queryType = QueryType.from(sql);
        queryCountByType.merge(queryType, 1, Integer::sum);
    }

}
