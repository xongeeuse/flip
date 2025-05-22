
# 🚀 AMR 경로 최적화 알고리즘 서버 - README (한국어)

> 본 서비스는 Kafka 메시지를 통해 AMR 상태를 수신하고, Redis와 MariaDB에서 데이터를 실시간으로 읽어온 후  
> **A\* 알고리즘**과 **헝가리안 알고리즘**을 활용하여 최적의 작업 할당과 경로를 계산하는 **Python 기반 알고리즘 마이크로서비스**입니다.

---
## 📖 알고리즘 서버 소개

스마트 팩토리 내 다수의 자율주행 로봇(AMR)들이 실시간으로 미션을 받고 최적 경로를 통해 작업을 수행할 수 있도록 하는 경로 계산 서버입니다.  
주요 목적은 **충돌 없는 경로 생성**, **배터리 기반 충전 유도**, **적재 지점 할당 최적화**이며, Kafka/Redis/MariaDB와 통합되어 작동합니다.

## 📁 폴더 구조

```
BE/
├── algorithm/
│   ├── AlgorithmServer.py     # Kafka 메시지 수신/처리, Redis 상태 확인 및 스케줄링
│   ├── api.py                 # A*, 헝가리안 알고리즘, DB 그래프 초기화 로직
│   ├── requirements.txt       # Python 의존성 목록
│   └── Dockerfile             # Docker 설정
```

---

## ✨ 주요 기능

| 항목 | 설명 | 위치 |
|------|------|------|
| ✅ Kafka 소비자/생산자 | `algorithm-trigger`, `algorithm-result` 주제 처리 | `AlgorithmServer.py` |
| ✅ Redis 조회 | `AMR_STATUS:*`, `MISSION_PT:*` 상태 실시간 확인 | `AlgorithmServer.py` |
| ✅ MariaDB 그래프 로딩 | `node`, `edge`, `mission` 테이블 조회 | `api.py → mapInit()` |
| ✅ A\* 알고리즘 | 노드 간 최단 경로 탐색 (속도 반영) | `api.py → aStar()` |
| ✅ 헝가리안 알고리즘 | 작업-로봇 최적 매칭 | `api.py → hungarian()` |
| ✅ 충전소 배정, 경로 차단 회피 | 배터리 수준·차단 간선 고려한 유연한 할당 | `api.py`, `AlgorithmServer.py` |

---

## 🛠️ 사용 기술

| 구분 | 기술 |
|------|------|
| 언어 | Python 3.11 |
| 메시징 | Kafka (confluent-kafka) |
| 실시간 상태 | Redis |
| DB | MariaDB (PyMySQL) |
| 알고리즘 | A\*, 헝가리안 (scipy.optimize), heapq |
| 기타 | Docker, python-dotenv, SimPy |

---

## ⚙️ 환경 변수 (.env 예시)

`.env` 파일을 아래와 같이 작성해주세요:

```env
# Kafka 설정
KAFKA_HOST=localhost
KAFKA_BOOT=localhost:9092

# MariaDB 설정
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=yourpassword
DB_NAME=flip
```

---

## 🧪 실행 방법

### 1. 로컬 실행

```bash
cd BE/algorithm
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python AlgorithmServer.py
```

### 2. Docker 실행

```bash
docker build -t flip-algo .
docker run -p 8083:8080 --env-file .env flip-algo
```

---

## 🔁 전체 동작 흐름

1. Kafka에서 `algorithm-trigger` 메시지를 수신합니다.
2. Redis에서 모든 AMR 상태 (`currentNode`, 배터리, 미션 등)를 확인합니다.
3. MariaDB에서 노드/간선 정보를 로딩하여 그래프를 생성합니다.
4. 주어진 조건에 따라 가능한 작업 목록을 구성합니다.
5. 헝가리안 알고리즘 (N^2logN) 으로 AMR ↔ 작업을 최적 매칭합니다.(cost : A*를 이용해서 거리 계산입니다.)
6. 적재 구역 예상 도착 시점과 다른 AMR이 경로를 참고해 가장 효율적인 적재 위치를 찾습니다.
7. A\* 알고리즘으로 실제 경로를 계산합니다.
8. Kafka에 `algorithm-result` 주제로 결과를 발행합니다.

---

## 🧩 DB 스키마 예시 [flip_backend를 실횅하면 DB는 자동으로 실행이 됩니다.]

```sql
CREATE TABLE node (
  node_id INT PRIMARY KEY,
  x DOUBLE, y DOUBLE
);

CREATE TABLE edge (
  edge_id INT PRIMARY KEY,
  edge_direction ENUM('twoway','forward','rearward'),
  speed DOUBLE,
  node1_node_id INT,
  node2_node_id INT
);

CREATE TABLE mission (
  mission_id INT PRIMARY KEY,
  mission_type ENUM('LOAD','UNLOAD','CHARGE','MOVE'),
  target_node_id INT
);
```

---

## 🧪 테스트 방법

Kafka에 메시지를 수동 전송하여 테스트할 수 있습니다:

```bash
echo '{"amrId":"AMR005","missionType":"MOVE"}' | \
kcat -P -b localhost:9092 -t algorithm-trigger
```

Redis 상태와 Kafka 출력, 경로 로그 등을 확인해보세요.

---

## 💡 브랜치 및 기여 규칙

- 브랜치 이름: `feat/297/algorithm-server`, `fix/310/hungarian-bug` 등
- 커밋 메시지 규칙: `feat:`, `fix:`, `refactor:` 등 명시
- PR 또는 MR은 반드시 `develop` 브랜치 기준으로 생성해주세요.

---

## 📜 라이선스

SSAFY 12기 S31팀 졸업 프로젝트 – 상업적 사용 금지
