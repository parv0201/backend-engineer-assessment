FROM gradle:8.7.0-jdk21 AS build

WORKDIR /workspace/app

COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN gradle clean build -x test

FROM eclipse-temurin:21-jre AS run

RUN adduser --system --group app

COPY --from=build --chown=app:app /workspace/app/build/libs/app-*.jar app.jar

EXPOSE 8080
USER app

CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]