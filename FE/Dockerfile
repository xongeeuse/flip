# 1. Node.js 베이스 이미지
FROM node:18

# 2. 컨테이너 내 앱 디렉토리 생성
WORKDIR /app

# 3. package.json과 lock 파일 복사
COPY package*.json ./

# 4. 의존성 설치
RUN npm install

# 5. 앱 전체 복사
COPY . .

# 6. 포트 노출 (Next.js dev server는 기본 3000번 포트)
EXPOSE 3000

# 7. 개발 서버 실행
CMD ["npm", "run", "dev"]
