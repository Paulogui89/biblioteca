# Use uma imagem base do OpenJDK
FROM openjdk:11-jdk-slim

# Defina o diretório de trabalho dentro do container
WORKDIR /app

# Copie o arquivo JAR gerado pelo Maven/Gradle para o diretório de trabalho
COPY target/biblioteca-1.0.0.jar app.jar

# Exponha a porta 8080 para acessar a aplicação
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]