#!/bin/bash

echo "🔍 Uruchamianie testów z JaCoCo coverage w kontenerze Docker..."

docker run --rm \
  -v "$(pwd)/booklovers:/app" \
  -w /app \
  -e SPRING_PROFILES_ACTIVE=test \
  -e MAVEN_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m" \
  maven:3.9.9-eclipse-temurin-21 \
         mvn clean verify -Dtest=!E2ETest

echo "✅ Testy zakończone!"
echo "📊 Raport coverage: booklovers/target/site/jacoco/index.html"