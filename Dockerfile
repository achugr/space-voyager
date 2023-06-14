FROM node:16.15.1 AS fontend-build
WORKDIR /app/client
COPY client/package*.json ./
RUN npm install
COPY client/ ./
RUN npm run build

FROM gradle:7-jdk11 AS fatjar-build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
COPY --from=fontend-build /app/build/client/ ./build/client/
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
RUN mkdir /app
COPY --from=fatjar-build /home/gradle/src/build/libs/voyager.jar /app/voyager.jar
ENTRYPOINT ["java","-jar","/app/voyager.jar", "-Dconfig.override_with_env_vars=true"]