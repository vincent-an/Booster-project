# Dockerfile

# 베이스 이미지로 Java 21이 설치된 이미지를 사용
FROM amazoncorretto:21

# .jar 파일이 위치할 컨테이너 내부의 작업 디렉토리 설정
WORKDIR /app

# 빌드된 .jar 파일을 컨테이너 내부로 복사
# build/libs/booster-0.0.1-SNAPSHOT.jar 파일을 app.jar 라는 이름으로 복사
COPY build/libs/booster-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]

