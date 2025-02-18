# Use uma imagem base do Maven para construção
FROM maven:3.8.4-openjdk-11 AS build

# Defina o diretório de trabalho dentro do container para a construção
WORKDIR /app

# Copie o arquivo pom.xml e as dependências para o cache
COPY pom.xml .
COPY src ./src

# Execute a construção do Maven para criar o arquivo JAR
RUN mvn clean package -DskipTests

# Use uma imagem base do OpenJDK para execução
FROM openjdk:11-jdk-slim

# Defina o diretório de trabalho dentro do container para a execução
WORKDIR /app

# Copie o arquivo JAR gerado pelo Maven para o diretório de trabalho
COPY --from=build /app/target/biblioteca-1.0.0.jar app.jar

# Exponha a porta 8080 para acessar a aplicação
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]