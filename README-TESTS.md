# Uruchamianie testów w Dockerze

## Opcja 1: Uruchomienie testów w kontenerze Maven (zalecane)

```bash
docker run --rm \
  -v "$(pwd)/booklovers:/app" \
  -w /app \
  -e SPRING_PROFILES_ACTIVE=test \
  maven:3.9.9-eclipse-temurin-21 \
  mvn clean test
```

Lub użyj skryptu:
```bash
./run-tests-docker.sh
```

## Opcja 2: Użycie skryptu

```bash
./run-tests-docker.sh
```

## Opcja 3: Uruchomienie konkretnych testów

```bash
# Wszystkie testy repozytoriów
docker run --rm \
  -v "$(pwd)/booklovers:/app" \
  -w /app \
  -e SPRING_PROFILES_ACTIVE=test \
  maven:3.9.9-eclipse-temurin-21 \
  mvn test -Dtest="*RepositoryTest"

# Konkretny test
docker run --rm \
  -v "$(pwd)/booklovers:/app" \
  -w /app \
  -e SPRING_PROFILES_ACTIVE=test \
  maven:3.9.9-eclipse-temurin-21 \
  mvn test -Dtest="UserRepositoryTest"
```

## Opcja 4: Użycie docker-compose (jeśli potrzebujesz bazy danych)

```bash
docker-compose -f docker-compose.test.yaml up --abort-on-container-exit
```

## Opcja 5: Uruchomienie testów w istniejącym kontenerze aplikacji

Jeśli kontener `booklovers-app` jest uruchomiony:

```bash
docker exec -it booklovers-app mvn test
```

**Uwaga:** To wymaga, aby kontener miał zainstalowany Maven (obecnie używa tylko JRE).

## Lokalnie (bez Dockera)

```bash
cd booklovers
mvn clean test
```
