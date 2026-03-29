# 1. Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copiamos el archivo que Maven generará según el nuevo pom.xml
COPY --from=build /app/target/healthflow-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]