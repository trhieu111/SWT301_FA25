package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class FormPage extends BasePage {

    public FormPage(WebDriver driver) {
        super(driver);
    }

    // Locators
    private By firstNameField = By.id("firstName");
    private By lastNameField = By.id("lastName");
    private By emailField = By.id("userEmail");
    private By genderMaleRadio = By.xpath("//label[text()='Male']");
    private By mobileField = By.id("userNumber");
    private By submitButton = By.id("submit");
    private By modalTitle = By.id("example-modal-sizes-title-lg");
    private By closeModalBtn = By.id("closeLargeModal");

    public void navigate() {
        navigateTo("https://demoqa.com/automation-practice-form");

        // ✅ 1. Xóa quảng cáo và banner che form
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('#fixedban')?.remove();" +
                        "document.querySelector('#adplus-anchor')?.remove();" +
                        "document.querySelector('footer')?.remove();" +
                        "document.querySelector('#close-fixedban')?.remove();"
        );

        // ✅ 2. Cuộn trang xuống để hiện form
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 250);");
    }

    public void fillForm(String first, String last, String email, String mobile) {
        // ✅ 3. Đợi phần tử thực sự render ra rồi mới nhập
        WebElement firstName = waitForVisibility(firstNameField);
        firstName.clear();
        firstName.sendKeys(first);

        type(lastNameField, last);
        type(emailField, email);
        click(genderMaleRadio);
        type(mobileField, mobile);
    }

    public void submitForm() {
        // ✅ Scroll đến nút submit (để tránh bị che)
        WebElement submit = driver.findElement(submitButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit);
        new Actions(driver).moveToElement(submit).click().perform();
    }

    public boolean isModalDisplayed() {
        try {
            return isElementVisible(modalTitle);
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
