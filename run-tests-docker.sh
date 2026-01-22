#!/bin/bash

echo "🔍 Uruchamianie testów z JaCoCo coverage w kontenerze Docker..."

docker run --rm \
  -v "$(pwd)/booklovers:/app" \
  -w /app \
  -e SPRING_PROFILES_ACTIVE=test \
  -e MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+UseStringDeduplication" \
  --memory=8g \
  maven:3.9.9-eclipse-temurin-21 \
         mvn clean verify -Dtest=!E2ETest 
        #  && mvn jacoco:report -q

echo "✅ Testy zakończone!"
echo "📊 Raport coverage: booklovers/target/site/jacoco/index.html"