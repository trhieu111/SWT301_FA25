package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import pages.FormPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Automation Practice Form Tests")
public class FormTest extends BaseTest {
    static FormPage formPage;

    @BeforeAll
    static void initPage() {
        formPage = new FormPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Should submit form successfully with valid data")
    void testSubmitFormSuccess() {
        formPage.navigate();
        formPage.fillForm("John", "Doe", "john.doe@gmail.com", "0987654321");
        formPage.submitForm();
        assertTrue(formPage.isModalDisplayed(), "Modal confirmation should appear after submission");
        formPage.closeModal();
    }

    @Test
    @Order(2)
    @DisplayName("Should fail when missing mandatory fields")
    void testSubmitFormMissingData() {
        formPage.navigate();
        formPage.fillForm("", "", "", "");
        formPage.submitForm();

        // Expect no modal appears when required fields are missing
        assertTrue(!formPage.isModalDisplayed(), "Modal should not appear for empty form");
    }
}
