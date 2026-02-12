# 🛒 Re-Market (리마켓)
> **신뢰를 기술로 증명하는 하이퍼 로컬(Hyper-local) 중고 거래 플랫폼**

---
## 📋 프로젝트 개요 (Overview)

### **"우리 동네, 가장 안전한 거래의 시작"**

기존 중고 거래 플랫폼의 고질적인 문제인 **허위 매물, 사기, 신뢰도 부족**을 해결하기 위해 기획된 위치 기반 커머스 서비스입니다.

**Re-Market**은 단순한 중개를 넘어, **엄격한 위치 인증**과 데이터 기반의 **안심 지수(Safety Score)** 시스템을 도입하여 '인증된 진짜 이웃' 간의 신뢰를 기술적으로 보장합니다. 대규모 트래픽 상황에서도 안정적인 **선착순 나눔**과 **실시간 채팅**을 제공하기 위해 정교한 동시성 제어 및 캐싱 최적화 기술이 적용되었습니다.

---

## 🎯 앱 주요 기능

### 1. 확실한 이웃 인증 및 커스텀 필터링 (Location Auth)
<table>
  <tr>
    <td align="center" width="50%">
      <img src="images/demo/location_1_.gif" alt="포트폴리오 목록 조회" width="70%"><br/>
      <p>내 동네 설정 (1)</p>
    </td>
    <td align="center" width="50%">
      <img src="images/demo/location_2_.gif" alt="포트폴리오 상세 조회" width="70%"><br/>
      <p>내 동네 설정 (2)</p>
    </td>
  </tr>
</table>

* **자동 리다이렉트**: 회원가입 후 초기 로그인 시 상품 조회가 불가능하며, 내 동네 설정 페이지로 자동 이동하여 필수 인증을 유도합니다.
* **GPS 위치 검증**: 현재 위치 좌표를 기반으로 행정동을 실시간 인증하고, 해당 위치 기준 반경 3km 이내의 매물을 즉시 조회합니다.
* **정교한 필터링**: 1.5km / 3km / 5km 거리 반경 설정과 가격대 설정, 정렬 필터(최신/저가/고가순)를 통해 맞춤형 매물 탐색이 가능합니다.


### 2. 중고 거래 및 무료 나눔 (Trading & Sharing)
<table>
  <tr>
    <td align="center" width="50%">
      <img src="images/demo/selling.gif" alt="포트폴리오 목록 조회" width="70%"><br/>
      <p>판매</p>
    </td>
    <td align="center" width="50%">
      <img src="images/demo/sharing.gif" alt="포트폴리오 상세 조회" width="70%"><br/>
      <p>나눔</p>
    </td>
  </tr>
</table>

* **간편한 등록**: 사진 업로드와 거래 희망 장소 설정을 통해 직관적으로 물건을 등록하고 관리할 수 있습니다.
* **자원 선순환**: 이웃에게 안 쓰는 물건을 나누는 '무료 나눔' 전용 카테고리를 제공하여 지역 커뮤니티 활성화를 돕습니다.
* **선착순 시스템**: 인기 나눔 품목의 정합성을 위해 동시성 제어 기술이 적용된 **선착순 신청** 시스템을 구축하였습니다.


### 3. 끊김 없는 실시간 채팅 (Real-time Chat)
<table>
  <tr>
    <td align="center" width="50%">
      <img src="images/demo/chatting.gif" alt="포트폴리오 목록 조회" width="90%"><br/>
      <p>채팅</p>
    </td>
  </tr>
</table>

* **실시간 인터렉션**: WebSocket(STOMP) 기반의 끊김 없는 메시지 전송으로 판매자와 구매자를 즉각 연결합니다.
* **유연한 거래 관리**: 대화 중 구매자의 요청에 따라 판매자가 게시글 상태를 `판매중`에서 `예약중`으로 즉시 변경하여 효율적인 거래 약속을 돕습니다.


### 4. 데이터 기반 신뢰 평가 (Safety Score)
<table>
  <tr>
    <td align="center">
      <img src="images/demo/score(1).gif" alt="판매 후기 및 점수 반영" width="90%"><br/>
      <p><b>판매 완료 후 신뢰도 평가 (사용자 A)</b></p>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="images/demo/score(2).gif" alt="나눔 후기 및 점수 반영" width="90%"><br/>
      <p><b>판매 완료 후 신뢰도 평가 (사용자 B)</b></p>
    </td>
  </tr>
</table>

* **안심 지수(Safety Score)**: 거래 완료 후 남겨진 상호 평가 결과를 데이터화하여 사용자의 신뢰도를 수치로 증명합니다.
* **객관적 지표**: 거래 후기, 매너 태그(시간 약속, 응답 속도 등)를 종합 분석하여 매너 좋은 이웃이 돋보이는 건강한 거래 환경을 조성합니다.

---

## 🏗️ 시스템 아키텍처

Re-Market은 대규모 트래픽 처리를 고려하여 **캐싱, 메시징, 모니터링** 계층이 분리된 아키텍처로 설계되었습니다.

| Component | Technology | Description |
| :--- | :--- | :--- |
| **App Server** | Spring Boot 3.4 | REST API 및 비즈니스 로직 처리 |
| **Main DB** | MySQL 8.0 | 회원, 상품, 거래 등 핵심 데이터 저장 |
| **Chat DB** | MongoDB | 대용량 채팅 로그 저장 (Write-Heavy 최적화) |
| **Cache/Lock** | Redis 7.0 | 조회 성능 캐싱, 분산 락, 인증 코드 저장 |
| **Monitoring** | Prometheus + Grafana | 시스템 지표 수집 및 시각화 |

---

## 🛠️ 기술 스택

| Category | Technology |
| :--- | :--- |
| **Language** | ![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white) |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) |
| **Infrastructure** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) ![Testcontainers](https://img.shields.io/badge/Testcontainers-231F20?style=flat-square&logo=testcontainers&logoColor=white) |
| **Tools & Utils** | ![QueryDSL](https://img.shields.io/badge/QueryDSL-007396?style=flat-square) ![Redisson](https://img.shields.io/badge/Redisson-Distributed_Lock-CC0000?style=flat-square) ![Actuator](https://img.shields.io/badge/Spring_Actuator-6DB33F?style=flat-square) |
| **Monitoring** | ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=flat-square&logo=prometheus&logoColor=white) ![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=grafana&logoColor=white) |

---

## 🚀 고도화 구현 기술 (Advanced Engineering)
> 대규모 트래픽 상황에서도 데이터 정합성을 유지하고 성능을 최적화하기 위해 적용한 기술들입니다.

### 1. Concurrency Control (동시성 제어)
- **문제:** 선착순 나눔(Sharing) 이벤트 시 재고보다 많은 요청이 성공하거나(Overselling), 동시에 리뷰 작성 시 점수 갱신이 누락되는 문제.
- **해결 & 성과:**
    - **Pessimistic Lock (비관적 락):** 선착순 나눔 재고 관리에 적용하여 100명의 동시 요청에도 정확히 재고 수만큼만 성공하도록 제어.
    - **Atomic Update:** 좋아요 수 집계 시 DB 레벨의 원자적 연산을 사용하여 100건의 동시 요청 시 손실율 0% 달성.
    - **Optimistic Lock (낙관적 락):** 리뷰 점수 갱신 시 충돌 감지 및 재시도 로직을 통해 데이터 정합성 보장.

### 2. Advanced Caching Strategy
- **문제:** 메인 페이지 조회 시 트래픽 폭주(Thundering Herd) 및 존재하지 않는 키에 대한 반복 요청(Cache Penetration)으로 DB 부하 발생.
- **해결 & 성과:**
    - **Cache Penetration Defense:** DB에 없는 데이터 요청 시 `Null Object`를 짧은 TTL로 캐싱하여 DB 히트율을 획기적으로 낮춤 (10회 반복 요청 시 DB 조회 1회 방어).
    - **Thundering Herd Protection:** 캐시 만료 시점에 분산 락(Distributed Lock)과 DCL(Double Checked Locking)을 적용하여 단 하나의 스레드만 DB에 접근하도록 제어 (30명 동시 요청 시 DB 조회 1회).

### 3. Hybrid Database Architecture
- **구조:** `MySQL`(관계형 데이터) + `MongoDB`(채팅 로그)
- **특징:**
    - 채팅방의 상태 및 참여자 정보는 트랜잭션이 중요한 MySQL에서 관리.
    - 빈번하게 생성되는 채팅 메시지는 스키마가 유연하고 쓰기 성능이 뛰어난 MongoDB에 저장하여 시스템 전체의 부하를 분산.

---

## 📊 테스트 및 성능 개선

### 동시성 제어 테스트 결과 (`ConcurrencySuccessTest`)
가상 시나리오를 통해 동시성 제어 로직의 유효성을 검증했습니다.

| 시나리오 | 적용 기술 | 요청 수 (Threads) | 결과 (Expected vs Actual) | 비고 |
| :--- | :--- | :--- | :--- | :--- |
| **선착순 나눔** | Pessimistic Lock | 100명 | **성공 10명 / 재고 0개** | 재고 초과 방지 성공 |
| **좋아요 집계** | Atomic Update | 100명 | **좋아요 100개** | 갱신 손실(Lost Update) 없음 |
| **안심 지수 갱신** | Optimistic Lock | 10명 | **상승량 25점** (Limit) | 충돌 시 재시도 로직 정상 작동 |

### 캐시 성능 방어 테스트 결과 (`CacheSuccessTest`)

| 테스트 항목 | 상황 | 적용 기술 | 결과 |
| :--- | :--- | :--- | :--- |
| **Cache Penetration** | 존재하지 않는 ID 반복 조회 (10회) | Null Object Pattern | **DB 조회 1회** (나머지 9회 캐시 차단) |
| **Thundering Herd** | 캐시 만료 직후 동시 접속 (30명) | Distributed Lock + DCL | **DB 조회 1회** (중복 쿼리 방지) |

---