#!/bin/bash


echo "🔍 Uruchamianie testów w kontenerze Docker..."

docker run --rm \
  -v "$(pwd)/booklovers:/app" \
  -w /app \
  -e SPRING_PROFILES_ACTIVE=test \
  maven:3.9.9-eclipse-temurin-21 \
  mvn clean test

echo "✅ Testy zakończone!"
