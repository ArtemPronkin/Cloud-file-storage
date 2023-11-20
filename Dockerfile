FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/filescloud.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/filescloud.jar"]