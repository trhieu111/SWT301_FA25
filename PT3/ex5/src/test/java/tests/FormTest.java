package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.FormPage;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Automation Practice Form Tests")
public class FormTest {

    static WebDriver driver;
    static FormPage formPage;

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // ⏳ implicit wait tăng lên 15s
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

        formPage = new FormPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Điền form hợp lệ → hiển thị modal thành công")
    void testSubmitFormSuccess() {
        formPage.navigate();
        formPage.fillForm("John", "Doe", "john@example.com", "0987654321");
        formPage.submitForm();
        assertTrue(formPage.isModalDisplayed(), "Modal không hiển thị!");
        formPage.closeModal();
    }

    @Test
    @Order(2)
    @DisplayName("Thiếu dữ liệu → modal không hiển thị")
    void testSubmitFormMissingData() {
        formPage.navigate();
        formPage.fillForm("", "Doe", "", "");
        formPage.submitForm();
        Assertions.assertFalse(formPage.isModalDisplayed(), "Modal không nên hiển thị khi thiếu dữ liệu!");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
