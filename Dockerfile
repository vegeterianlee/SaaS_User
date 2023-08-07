# Start with a base image containing Java runtime
FROM adoptopenjdk:11-jdk-hotspot as build

# Arguments that will be used for providing environment variables
ARG JWT_SECRET_KEY
ARG DATABASE_DDL_AUTO
ARG DATABASE_DIALECT
ARG DATABASE_DRIVER
ARG DATABASE_URL
ARG DATABASE_USERNAME
ARG DATABASE_PASSWORD
ARG ADMIN_TOKEN

# Environment variables
ENV JWT_SECRET_KEY=$JWT_SECRET_KEY
ENV DATABASE_DDL_AUTO=$DATABASE_DDL_AUTO
ENV DATABASE_DIALECT=$DATABASE_DIALECT
ENV DATABASE_DRIVER=$DATABASE_DRIVER
ENV DATABASE_URL=$DATABASE_URL
ENV DATABASE_USERNAME=$DATABASE_USERNAME
ENV DATABASE_PASSWORD=$DATABASE_PASSWORD
ENV ADMIN_TOKEN=$ADMIN_TOKEN

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Set the working directory in the image
WORKDIR /app

# Copy the Gradle executable to the image
COPY gradlew .
RUN chmod +x ./gradlew
COPY gradle gradle

# Copy the build.gradle file
COPY build.gradle .

# Copy the source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test --stacktrace

# Run Stage
FROM adoptopenjdk:11-jdk-hotspot

EXPOSE 8080

WORKDIR /app

# Copy the built jar file
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]