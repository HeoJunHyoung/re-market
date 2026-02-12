import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    // [부하 시나리오 설정]
    // 1. Ramp-up: 10초 동안 사용자(VU)를 0명에서 10명까지 늘림
    // 2. Steady: 30초 동안 100명의 사용자가 지속적으로 요청 (Thundering Herd 시뮬레이션)
    // 3. Ramp-down: 10초 동안 사용자를 0명으로 줄임
    stages: [
        { duration: '10s', target: 10 },
        { duration: '30s', target: 100 },
        { duration: '10s', target: 0 },
    ],
    // [성공 기준 설정]
    // 95%의 요청이 500ms 이내에 응답해야 하며, 에러율은 1% 미만이어야 함
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    // [테스트 대상 URL]
    // 포트 8000번 적용, page=0 조건 (캐싱 타겟)
    const url = 'http://localhost:8000/items?page=0&size=20';

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            name: 'MainPageItemSearch', // 결과 분석 시 태그로 구분 가능
        },
    };

    const res = http.get(url, params);

    if (res.status !== 200) {
        console.error(`Error: ${res.status} - ${res.body}`);
    }

    // [응답 검증]
    // 상태 코드가 200인지 확인
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    // [휴식 시간]
    // 실제 사용자의 행동 패턴을 모방하기 위해 0.1초 대기
    sleep(0.1);
}