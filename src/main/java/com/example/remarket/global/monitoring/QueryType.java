package com.example.remarket.global.monitoring;

import lombok.Getter;

@Getter
public enum QueryType { // SQL 종류 구분
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    OTHER;

    public static QueryType from(String sql) {
        if (sql == null || sql.isBlank()){
            return OTHER;
        }
        String firstWord = sql.trim().split("\\s+")[0].toUpperCase();
        try {
            return QueryType.valueOf(firstWord);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
