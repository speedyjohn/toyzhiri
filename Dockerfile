FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM nginx:alpine
WORKDIR /app

RUN apk add --no-cache openjdk17-jre

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN mkdir -p /app/uploads && chown -R spring:spring /app

EXPOSE 80

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "su spring -s /bin/sh -c 'java $JAVA_OPTS -jar /app/app.jar &' && nginx -g 'daemon off;'"]