# 📚 Book Lovers Community

Aplikacja społecznościowa dla czytelników - katalogowanie książek, recenzje, oceny i statystyki.

## 🚀 Uruchomienie aplikacji

### Opcja 1: Docker Compose (zalecane)

```bash
# Uruchomienie bazy Oracle i aplikacji
docker-compose up -d

# Sprawdzenie logów
docker-compose logs -f booklovers-app

# Zatrzymanie
docker-compose down
```

Aplikacja będzie dostępna pod adresem: http://localhost:8080

### Opcja 2: Lokalnie (z H2 dla testów)

```bash
# Zmień profil na h2 w application.yaml (linia 5: active: h2)
# Uruchom aplikację
cd booklovers
mvn spring-boot:run
```

## 🧪 Testowanie aplikacji

### 1. Rejestracja użytkownika
- Przejdź na: http://localhost:8080/register
- Utwórz konto (np. username: `admin`, password: `admin123`)
- Zaloguj się

### 2. Utworzenie użytkownika z rolą ADMIN (dla dodawania książek)

Domyślnie nowi użytkownicy mają rolę USER. Aby dodać książki, możesz:

**Opcja A:** Zmień rolę w bazie danych:
```sql
-- Połącz się z Oracle
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
```

**Opcja B:** Dodaj książki przez REST API:
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "title": "Hobbit",
    "author": "J.R.R. Tolkien",
    "isbn": "978-83-240-0000-0",
    "description": "Klasyczna powieść fantasy"
  }'
```

### 3. Dodawanie książek przez UI (wymaga roli ADMIN)
- Zaloguj się jako admin
- Przejdź na: http://localhost:8080/books
- Kliknij "Dodaj książkę" (widoczne tylko dla admina)
- Wypełnij formularz i zapisz

### 4. Dodawanie książek do biblioteczki
- Przejdź do szczegółów książki: http://localhost:8080/books/{id}
- Kliknij "Dodaj do biblioteczki"
- Wybierz kategorię (np. "Do przeczytania", "Przeczytane")

### 5. Dodawanie recenzji i ocen
- Na stronie szczegółów książki dodaj recenzję
- Użyj REST API do dodania oceny:
```bash
curl -X POST http://localhost:8080/api/ratings/books/1 \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"value": 5}'
```

## 📋 Endpointy REST API

### Autoryzacja
- `POST /api/auth/register` - Rejestracja
- `POST /api/auth/login` - Logowanie

### Książki
- `GET /api/books` - Lista wszystkich książek
- `GET /api/books/{id}` - Szczegóły książki
- `POST /api/books` - Dodaj książkę (wymaga autoryzacji)
- `POST /api/books/{id}/add-to-library?shelfName=Kategoria` - Dodaj do biblioteczki

### Recenzje
- `POST /api/reviews/books/{bookId}` - Dodaj recenzję
- `GET /api/reviews/books/{bookId}` - Lista recenzji dla książki

### Oceny
- `POST /api/ratings/books/{bookId}` - Dodaj/aktualizuj ocenę (1-5)

### Statystyki
- `GET /api/stats/user` - Statystyki użytkownika
- `GET /api/stats/books` - Statystyki globalne

## 🗄️ Baza danych

### Oracle (produkcja)
- Host: `oracle-db` (w Docker) lub `localhost:1521` (lokalnie)
- Database: `XEPDB1`
- Domyślny użytkownik: `admin` / hasło: `oracle` (lub zgodnie z docker-compose.yaml)

### H2 (testy)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:bookloversdb`
- Username: `sa`
- Password: (puste)

## 🛠️ Technologie

- Java 21
- Spring Boot 3.5.8
- Spring Security
- Spring Data JPA
- Thymeleaf
- Oracle XE / H2
- Maven

## 📝 Uwagi

- Domyślnie aplikacja używa profilu `oracle`
- Dla testów zmień profil na `h2` w `application.yaml`
- Aby dodać książki przez UI, użytkownik musi mieć rolę `ADMIN`
- Książki można dodawać również przez REST API bez roli admin
