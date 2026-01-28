import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    scenarios: {
        // [시나리오 1] Cache Penetration: 존재하지 않는 랜덤 ID 무차별 대입
        penetration_attack: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 20 },
                { duration: '20s', target: 20 }, // 20명이 지속적으로 DB 없는 데이터 요청
                { duration: '5s', target: 0 },
            ],
            exec: 'penetrationTest',
        },
        // [시나리오 2] Thundering Herd: 메인 페이지 집중 공격 (TTL 5초마다 스파이크 확인)
        thundering_herd: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 50 }, // 50명까지 증가
                { duration: '30s', target: 50 }, // 30초간 유지 (약 6번의 캐시 만료 사이클 발생)
                { duration: '5s', target: 0 },
            ],
            exec: 'mainPageTest',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 1초 넘어가면 실패로 간주
    },
};

const BASE_URL = 'http://localhost:8000'; // 로컬 서버 포트 확인 필요

export function penetrationTest() {
    // 100만 ~ 1억 사이 랜덤 ID (DB에 없을 확률 높음)
    const randomId = randomIntBetween(1000000, 100000000);
    const res = http.get(`${BASE_URL}/items/${randomId}`);

    // 404가 떠도 DB 조회를 했는지 확인하는 것이 목적
    check(res, {
        'status is 404 or 400': (r) => r.status === 404 || r.status === 400,
    });
    sleep(0.5);
}

export function mainPageTest() {
    // 메인 페이지 목록 조회
    const res = http.get(`${BASE_URL}/items?page=0&size=20`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    // 0.1초 간격으로 매우 빠르게 재요청 (동시성 유발)
    sleep(0.1);
}