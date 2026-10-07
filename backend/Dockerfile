FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B


FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# O FotoProdutoService grava em uploads/produtos; a pasta precisa existir antes
# do start, senão o primeiro upload depende do createDirectories.
RUN mkdir -p /app/uploads/produtos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
