package automation.pages;

import automation.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private By usernameInput = By.name("username");
    private By passwordInput = By.name("password");
    private By loginBtn = By.cssSelector("button[type='submit']");
    private By errorMsg = By.cssSelector(".text-danger, .alert-danger"); // tuỳ lỗi xuất hiện

    public LoginPage(WebDriver driver) { super(driver); }

    public void navigate() {
        driver.get("http://localhost:8080/auth/login");
    }

    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        click(loginBtn);
    }

    public boolean isError() {
        return isElementVisible(errorMsg);
    }
}



