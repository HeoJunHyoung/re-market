//package com.example.secondhandmarket.global.config;
//
//import org.hibernate.boot.model.FunctionContributions;
//import org.hibernate.dialect.MySQLDialect;
//import org.hibernate.query.sqm.function.SqmFunctionRegistry;
//
//public class MySQLCustomDialect extends MySQLDialect {
//
//    @Override
//    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
//        super.initializeFunctionRegistry(functionContributions);
//
//        SqmFunctionRegistry registry = functionContributions.getFunctionRegistry();
//
//        // "match"라는 함수를 호출하면 MySQL의 "match(?1) against(?2)" 문법으로 변환하도록 등록
//        registry.registerPattern("match", "match(?1) against (?2)");
//    }
//}