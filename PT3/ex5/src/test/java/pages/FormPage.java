package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class FormPage extends BasePage {

    private By firstNameField = By.id("firstName");
    private By lastNameField = By.id("lastName");
    private By emailField = By.id("userEmail");
    private By genderMaleRadio = By.xpath("//label[text()='Male']");
    private By mobileField = By.id("userNumber");
    private By submitButton = By.id("submit");
    private By modalTitle = By.id("example-modal-sizes-title-lg");
    private By closeModalBtn = By.id("closeLargeModal");

    public FormPage(WebDriver driver) {
        super(driver);
    }

    public void navigate() {
        driver.get("https://demoqa.com/automation-practice-form");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        // 🧹 Xóa tất cả quảng cáo / iframe / banner
        js.executeScript("""
            document.querySelectorAll('iframe, #fixedban, #adplus-anchor, footer').forEach(e => e.remove());
        """);

        // ⏳ Đợi firstName có trong DOM lâu hơn
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(firstNameField));

        // 💤 Nghỉ nhẹ để trang ổn định
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        js.executeScript("window.scrollTo(0, 250);");
    }

    public void fillForm(String first, String last, String email, String mobile) {
        try {
            WebElement firstName = waitForVisibility(firstNameField);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstName);
            firstName.clear();
            firstName.sendKeys(first);
        } catch (Exception e) {
            throw new RuntimeException("Không thể thao tác với field First Name", e);
        }

        type(lastNameField, last);
        type(emailField, email);
        click(genderMaleRadio);
        type(mobileField, mobile);
    }

    public void submitForm() {
        WebElement submit = driver.findElement(submitButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit);
        new Actions(driver).moveToElement(submit).click().perform();
    }

    public boolean isModalDisplayed() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(modalTitle))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void closeModal() {
        if (isElementVisible(closeModalBtn)) {
            click(closeModalBtn);
        }
    }
}
