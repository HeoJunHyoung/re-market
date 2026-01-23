package com.example.secondhandmarket.global.monitoring;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;


@Component
public class QueryCountInspector implements StatementInspector { // // Hibernate 쿼리 가로채기
    @Override
    public String inspect(String sql) {
        RequestContext ctx = RequestContextHolder.getContext();
        if (ctx != null) {
            ctx.incrementQueryCount(sql);
        }
        return sql;
    }
}