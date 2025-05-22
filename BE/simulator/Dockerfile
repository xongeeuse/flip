# 베이스 이미지 선택
FROM python:3.11-slim

# 작업 디렉토리 설정 (선택 사항이지만 권장됨)
WORKDIR /app

# 의존성 설치를 위해 requirements.txt 먼저 복사
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

# Python 파일 복사
COPY . .

# 컨테이너 실행 시 실행할 명령
CMD ["python", "simulator_client_v2.py"]