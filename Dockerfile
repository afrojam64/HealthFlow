# 1. Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# ¡ESTA ES LA MAGIA!: Buscamos y copiamos EXACTAMENTE el archivo correcto
COPY --from=build /app/target/HealthFlow-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]