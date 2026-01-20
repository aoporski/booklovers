package com.booklovers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class E2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private String baseUrl;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        driver = new ChromeDriver(options);
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testLoginPageLoads() {
        driver.get(baseUrl + "/login");

        String pageTitle = driver.getTitle();
        assertNotNull(pageTitle, "Tytuł strony nie powinien być null");
        assertFalse(pageTitle.isEmpty(), "Tytuł strony nie powinien być pusty");
        
        WebElement usernameInput = driver.findElement(By.name("username"));
        assertTrue(usernameInput.isDisplayed(), "Pole username powinno być widoczne");

        WebElement passwordInput = driver.findElement(By.name("password"));
        assertTrue(passwordInput.isDisplayed(), "Pole password powinno być widoczne");
    }

    @Test
    void testSwaggerUILoads() {
        driver.get(baseUrl + "/swagger-ui.html");

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("swagger") || currentUrl.contains("api-docs"),
                "URL powinien zawierać 'swagger' lub 'api-docs'. Aktualny URL: " + currentUrl);
    }

    @Test
    void testBooksPageLoads() {
        driver.get(baseUrl + "/books");

        String pageTitle = driver.getTitle();
        assertNotNull(pageTitle, "Tytuł strony książek nie powinien być null");
        assertFalse(pageTitle.isEmpty(), "Tytuł strony książek nie powinien być pusty");
        
        WebElement searchInput = driver.findElement(By.cssSelector("input[name='search']"));
        assertTrue(searchInput.isDisplayed(), "Pole wyszukiwania powinno być widoczne");
    }
}
