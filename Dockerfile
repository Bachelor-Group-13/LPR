FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

COPY . .
RUN chmod +x ./mvnw \
 && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

