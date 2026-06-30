import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        thundering_herd: {
            executor: 'constant-vus',
            vus: 100,           // 100명이 동시에
            duration: '5s',     // 5초 동안 쉬지 않고 요청
            exec: 'mainPage',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000'],
    },
};

export function mainPage() {
    // 토큰 없이 비로그인 요청 → 무조건 글로벌 메인 페이지 (캐시 대상)
    const res = http.get('http://localhost:8000/items?page=0&size=20', {
        headers: { 'Content-Type': 'application/json' },
    });

    check(res, {
        'status 200': (r) => r.status === 200,
    });
}