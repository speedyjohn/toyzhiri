# Многоэтапная сборка для оптимизации размера образа

# Этап 1: Сборка приложения
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Создание финального образа
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаём пользователя
RUN addgroup -S spring && adduser -S spring -G spring

# Копируем jar
COPY --from=build /app/target/*.jar app.jar

# Создаём директорию и сразу выдаём права пользователю
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Теперь переключаемся на непривилегированного пользователя
USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]