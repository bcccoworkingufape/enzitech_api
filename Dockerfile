# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e descarrega as dependências primeiro (aproveita o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte e compila gerando o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia EXATAMENTE apenas o .jar gerado no Estágio 1
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para iniciar a API
ENTRYPOINT ["java", "-jar", "app.jar"]