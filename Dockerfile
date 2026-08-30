# Build the Spring Boot API with the Java version declared in pom.xml.
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV TZ=Asia/Kolkata

COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
# Use shell form so $PORT is expanded at runtime from Render's injected env variable
CMD ["sh", "-c", "java -Duser.timezone=Asia/Kolkata -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
