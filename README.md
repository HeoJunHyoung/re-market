# 🛒 Re-Market

<h3 style="margin-top: 0;">
🤝 하이퍼 로컬 중고 거래 플랫폼</h3>


---

## 📋 프로젝트 개요
### **"우리 동네, 믿을 수 있는 거래의 시작 Re-Market"**

기존 중고 거래 플랫폼에서 발생하는 **허위 매물, 사기, 그리고 신뢰도 부족 문제**를 해결하기 위해 탄생했습니다. **Re-Market**은 **엄격한 위치 기반 인증**과 **투명한 매너 평가 시스템**을 통해 사용자 간의 신뢰를 기술적으로 보장하는 하이퍼로컬(Hyper-local) 커머스 플랫폼입니다.

단순한 상품 거래를 넘어, **무료 나눔(Sharing)** 기능을 통해 자원의 선순환을 돕고, 실시간 채팅과 정교한 동시성 제어 기술을 도입하여 대규모 트래픽 상황에서도 안정적인 거래 경험을 제공합니다.

---

## 🎯 앱 주요 기능

### 1. 동네 인증 및 회원 관리
* **위치 기반 동네 인증**: GPS를 이용하여 사용자의 실제 위치가 설정한 동네(행정동) 반경 3km 이내인지 검증합니다. 인증된 '진짜 이웃'끼리만 거래할 수 있어 신뢰도를 높였습니다.
* **SMS 본인 인증**: 회원가입 시 휴대폰 SMS 인증을 통해 허위 계정 생성을 방지하고, 1인 1계정 원칙을 강화했습니다.

### 2. 중고 거래 및 무료 나눔
* **상품 등록 및 관리**: 판매하고자 하는 물품의 사진, 가격, 상세 설명을 등록하고 거래 희망 장소를 설정할 수 있습니다.
* **무료 나눔(Sharing)**: 안 쓰는 물건을 이웃에게 무료로 나누는 전용 카테고리를 제공합니다. 선착순 신청 기능을 통해 투명하게 나눔 대상자를 선정할 수 있습니다.
* **거래 상태 관리**: `판매중` ↔ `예약중` ↔ `거래완료` 상태를 실시간으로 변경하여 구매자들에게 정확한 정보를 제공합니다.

### 3. 신뢰 평가 시스템 (안심 지수)
* **안심 지수(Safety Score)**: 사용자의 거래 매너를 수치화한 점수입니다. 거래 후기, 평가 태그(친절해요, 시간 약속을 잘 지켜요 등), 거래 빈도를 종합적으로 분석하여 점수가 변동됩니다.
* **거래 후기**: 거래가 완료된 후 상대방에 대한 후기와 별점(안전/보통/위험)을 남길 수 있어, 다음 거래자에게 유용한 정보를 제공합니다.

### 4. 실시간 1:1 채팅
* **실시간 대화**: 마음에 드는 상품이 있다면 판매자와 즉시 1:1 대화를 시작할 수 있습니다. 별도의 새로고침 없이 메시지가 실시간으로 전송됩니다.
* **거래 연동**: 채팅방 상단에 거래 중인 물품 정보가 표시되어, 어떤 상품에 대한 대화인지 직관적으로 파악할 수 있습니다.

### 5. 관심 상품 및 검색
* **찜하기(관심 상품)**: 사고 싶은 물건을 관심 상품으로 등록해두면 가격 변동이나 상태 변경 시 모아볼 수 있습니다.
* **맞춤 검색**: 카테고리, 가격대, 동네 설정 등 다양한 필터를 통해 원하는 물건을 쉽고 빠르게 찾을 수 있습니다.

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