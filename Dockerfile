FROM eclipse-temurin:17-jdk-alpine AS base
WORKDIR app/
COPY to-do-list .

FROM base AS build
RUN ["./gradlew", "build"]

FROM eclipse-temurin:17-jre-alpine AS dev
WORKDIR app/
COPY --from=build /app/build/libs/to-do-list-*.jar ./to-do-list.jar
EXPOSE 8080
EXPOSE 5005
CMD ["java", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "to-do-list.jar"]

FROM eclipse-temurin:17-jre-alpine AS master
WORKDIR app/
COPY --from=build /app/build/libs/to-do-list-*.jar ./to-do-list.jar
EXPOSE 8080
CMD ["java", "-jar", "to-do-list.jar"]