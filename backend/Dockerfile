# ---------- Étape 1 : build avec Maven ----------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /app
# Les dépendances d'abord : cette couche reste en cache tant que pom.xml ne change pas
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Étape 2 : image d'exécution légère ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
# Ne jamais exécuter l'application en root dans un conteneur
RUN groupadd --system secutrack && useradd --system --gid secutrack secutrack
COPY --from=build /app/target/*.jar app.jar
USER secutrack
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
