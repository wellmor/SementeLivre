FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# O FotoProdutoService grava em uploads/produtos; a pasta precisa existir antes
# do start, senão o primeiro upload depende do createDirectories.
RUN mkdir -p /app/uploads/produtos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
