package com.example.secondhandmarket.global.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;

public class MySQLCustomDialect extends MySQLDialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        SqmFunctionRegistry registry = functionContributions.getFunctionRegistry();

        // 1. 기본 Match (Natural Language Mode)
        registry.registerPattern("match", "match(?1) against (?2)");

        // 2. Boolean Mode Match (추가)
        registry.registerPattern("match_boolean", "match(?1) against (?2 in boolean mode)");
    }
}