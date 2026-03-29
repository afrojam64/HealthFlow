# 1. Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# Borramos el jar 'plain' para que solo quede el ejecutable real
RUN rm -f target/*-plain.jar

# 2. Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Usamos el comodín *.jar para que no importe el nombre exacto
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]