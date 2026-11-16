# Compilacao Maven em container (nao depende do Maven instalado no Windows).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# Imagem de execucao menor, somente com Java 17.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/portal-academico-1.0.0.jar /app/app.jar
# Limita o heap para deixar margem ao JSF, threads e memoria nativa (plano Free: 512 MB).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -XX:+ExitOnOutOfMemoryError"
USER 10001:10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
