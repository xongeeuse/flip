# 📚 포팅 매뉴얼 (Updated)

## 📑 목차

1. [기술 스택 & 버전 정보](#1-기술-스택--버전-정보)  
2. [빌드 방법](#2-빌드-방법)  
3. [환경 변수](#3-환경-변수)  
4. [외부 서비스 정보](#4-외부-서비스-정보)  
5. [포트 매핑 요약](#5-포트-매핑-요약)  
6. [기타 설정](#6-기타-설정)

---

## 1. 기술 스택 & 버전 정보

| 서버                      | 프레임워크 / 런타임        | 언어           | 버전 / 베이스 이미지            |
| ------------------------- | ------------------------- | ------------- | ----------------------------- |
| flip_backend              | Spring Boot               | Java          | JDK 21, Gradle 8.5             |
| flip_web                  | Spring Boot               | Java          | JDK 21, Gradle 8.5             |
| AlgorithmServer           | —                         | Python        | 3.11-slim                      |
| Frontend (React)          | Next.js                   | Node.js       | React 19.0.0, Next 15.3.1      |
| Simulation Client         | —                         | Python        | 3.13                           |
| Kafka                     | Confluent Platform        | —             | Kafka 7.3.0, Zookeeper 7.3.0   |
| DB                        | —                         | —             | MariaDB 10.7                   |
| 캐시                      | —                         | —             | Redis 7                        |

---

## 2. 빌드 방법

### ✅ flip_backend (Spring Boot)

1. 프로젝트 디렉토리로 이동 (`flip_backend/`)  
2. JDK 21 버전 설치 및 `JAVA_HOME` 설정 확인  
3. 빌드 수행  
   ```bash
   ./gradlew clean build
   ```  
4. 생성된 JAR 실행 (예)  
   ```bash
   java -jar build/libs/flip-backend-0.0.1-SNAPSHOT.jar
   ```  
5. Docker 이미지 빌드 & 실행  
   ```bash
   docker build -t flip_backend .
   docker run -d -p 8080:8080      -e custom.spring.datasource.url=jdbc:mariadb://<DB_HOST>:<DB_PORT>/flip      -e custom.spring.datasource.username=<DB_USER>      -e custom.spring.datasource.password=<DB_PASSWORD>      -e custom.spring.redis.host=<REDIS_HOST>      -e custom.spring.redis.port=<REDIS_PORT>      -e custom.kafka.bootstrap-server=<KAFKA_BOOTSTRAP>      flip_backend
   ```

### ✅ flip_web (Spring Boot)

1. 프로젝트 디렉토리로 이동 (`flip_web/`)  
2. JDK 21 설치 확인  
3. 빌드  
   ```bash
   ./gradlew clean build
   ```  
4. JAR 실행  
   ```bash
   java -jar build/libs/flip-web-0.0.1-SNAPSHOT.jar
   ```  
5. Docker  
   ```bash
   docker build -t flip_web .
   docker run -d -p 8081:8080      -e custom.spring.datasource.url=jdbc:mariadb://<DB_HOST>:<DB_PORT>/flip      -e custom.spring.datasource.username=<DB_USER>      -e custom.spring.datasource.password=<DB_PASSWORD>      -e custom.spring.redis.host=<REDIS_HOST>      -e custom.spring.redis.port=<REDIS_PORT>      -e custom.kafka.bootstrap-server=<KAFKA_BOOTSTRAP>      flip_web
   ```

### ✅ AlgorithmServer (Python)

1. 프로젝트 디렉토리로 이동 (`AlgorithmServer/`)  
2. Docker 이미지 빌드 & 실행  
   ```bash
   docker build -t algorithm_server .
   docker run -d      -e KAFKA_BOOT=<KAFKA_BOOTSTRAP>      -e KAFKA_HOST=<KAFKA_HOST>  algorithm_server
   ```  
   - 의존성 설치: `requirements.txt` 사용  
   - 실행 명령: `python AlgorithmServer.py`

### ✅ Frontend (React / Next.js)

1. 프로젝트 디렉토리로 이동 (`frontend/`)  
2. 패키지 설치  
   ```bash
   npm install
   ```  
3. 빌드  
   ```bash
   npm run build
   ```  
4. Docker 이미지 빌드 & 실행  
   ```bash
   docker build -t flip_frontend .
   docker run -d -p 3000:3000 flip_frontend
   ```

### ✅ Simulation Client (Python)

1. Python 3.13 설치 확인  
2. 의존성 설치  
   ```bash
   pip install simpy websocket-client python-dotenv
   ```  
3. 실행  
   ```bash
   python simulator_client_v2.py
   ```

---

## 3. 환경 변수

### ✅ flip_backend

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `custom.spring.datasource.url` | DB 접속 URL | `jdbc:mariadb://localhost:3307/flip` |
| `custom.spring.datasource.username` | DB 사용자 | `root` |
| `custom.spring.datasource.password` | DB 비밀번호 | `1234` |
| `custom.spring.redis.host` | Redis 호스트 | `localhost` |
| `custom.spring.redis.port` | Redis 포트 | `6379` |
| `custom.kafka.bootstrap-server` | Kafka 주소 | `localhost:9092` |

### ✅ flip_web

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `custom.kafka.bootstrap-server` | Kafka 주소 | `localhost:9092` |

### ✅ AlgorithmServer

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `KAFKA_BOOT` | Kafka bootstrap 주소 | `localhost:9092` |
| `DB_HOST` | DB 주소 | `localhost` |
| `DB_PORT` | 포트 | `3307` |
| `DB_USER` | DB 사용자 | `root` |
| `DB_PASSWORD` | 비밀번호 | `1234` |
| `DB_NAME` | DB 이름 | `flip` |

---

## 4. 외부 서비스 정보

- **MariaDB**: 미션 기록, AMR 상태 저장
- **Redis**: 실시간 상태 공유
- **Kafka**: AMR 상태 스트리밍 및 이벤트 브로커
- **WebSocket**: 사람/AMR 실시간 통신
- **Docker Registry**: (선택) 이미지 배포

---

## 5. 포트 매핑 요약

| 서비스        | 내부 포트 | 외부 포트 | 설명                                      |
|---------------|-----------|-----------|-------------------------------------------|
| flip_backend  | 8080      | 8081      | REST API 서버                             |
| flip_web      | 8080      | 8082      | WebSocket API 서버                        |
| flip_algo     | 8080      | 8083      | Python 알고리즘 서버                      |
| flip_frontend | 3000      | 3000      | Next.js 프론트엔드                        |
| simulator     | 8080      | 8084      | Python 시뮬레이터                         |
| MariaDB       | 3306      | 3306      | DB 서버                                   |
| Redis         | 6379      | 6379      | Redis 캐시 서버                           |
| Kafka         | 9092      | 9092      | Kafka 외부용 접속 (PLAINTEXT)            |
| Kafka         | 29092     | internal  | Kafka 내부 통신 (INTERNAL)                |
| Zookeeper     | 2181      | 2181      | Kafka 메타데이터 관리용 Zookeeper 포트    |


---

## 6. 기타 설정

- Kafka Listener Concurrency:  
  `spring.kafka.listener.concurrency=8`

- SimPy 설정:  
  factor = 0.35, WebSocket 클라이언트 = 20개

- 로그 설정:  
  - Java: `logback-spring.xml`  
  - Python: `print` → 확장 시 `logging` 사용 권장

---