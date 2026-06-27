# Usa apenas a imagem do JRE (ambiente de execução)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o .jar que o GitHub Actions acabou de gerar
COPY target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para iniciar a API
ENTRYPOINT ["java", "-jar", "app.jar"]