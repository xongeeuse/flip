# 1. 사용할 베이스 이미지
FROM openjdk:21-jdk-slim

# 2. JAR 파일을 컨테이너에 복사
COPY app.jar /app.jar

# 3. 컨테이너가 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]
