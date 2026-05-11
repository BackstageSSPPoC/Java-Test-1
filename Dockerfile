FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /usr/src/app

COPY pom.xml .
COPY Main.java .          # ← src ./src nahi, sirf Main.java

RUN mvn package -DskipTests -B   # dependency:go-offline hata do — no deps hain

FROM eclipse-temurin:17-jre-alpine

WORKDIR /usr/src/app

COPY --from=builder /usr/src/app/target/app.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
