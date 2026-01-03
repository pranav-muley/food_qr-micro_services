# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and pom first (better cache)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw -B -q dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -B -q -DskipTests package

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-Xms128m -Xmx256m"

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
