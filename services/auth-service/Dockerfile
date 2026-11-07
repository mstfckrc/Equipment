FROM eclipse-temurin:25.0.5_8-jdk-noble AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25.0.5_8-jre-noble

WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/auth-service-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
