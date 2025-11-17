package automation.pages;

import automation.core.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SignupPage extends BasePage {

    private By usernameInput = By.name("username");
    private By emailInput = By.name("email");
    private By passwordInput = By.name("password");
    private By signupBtn = By.cssSelector("button[type='submit']");
    private By successMsg = By.cssSelector("p.text-success");
    private By errorMsg = By.cssSelector("p.text-danger");

    public SignupPage(WebDriver driver) { 
        super(driver); 
    }

    public void navigate() {
        driver.get("http://localhost:8080/auth/signup");
        wait.until(ExpectedConditions.presenceOfElementLocated(signupBtn));
    }

    public void signup(String username, String email, String password) {
        try {
            System.out.println("\n=== SIGNUP: " + username + ", " + email + " ===");
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).clear();
            driver.findElement(usernameInput).sendKeys(username);
            driver.findElement(emailInput).clear();
            driver.findElement(emailInput).sendKeys(email);
            driver.findElement(passwordInput).clear();
            driver.findElement(passwordInput).sendKeys(password);
            
            WebElement submitBtn = driver.findElement(signupBtn);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();", submitBtn);
            
            Thread.sleep(4000);
            
        } catch (Exception e) {
            System.out.println("Signup error: " + e.getMessage());
        }
    }
    
    public boolean isSuccess() {
        try {
            Thread.sleep(1000);
            String text = driver.findElement(successMsg).getText();
            String pageSource = driver.getPageSource();
            boolean result = (text != null && text.contains("✅")) || pageSource.contains("✅ Vui lòng kiểm tra email");
            System.out.println("SUCCESS: " + result);
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isError() {
        try {
            Thread.sleep(1000);
            String text = driver.findElement(errorMsg).getText();
            String pageSource = driver.getPageSource();
            String url = driver.getCurrentUrl();
            
            boolean hasErrorMsg = (text != null && text.contains("❌")) || pageSource.contains("❌");
            boolean noSuccess = !pageSource.contains("✅ Vui lòng kiểm tra email");
            boolean stayedOnPage = url.contains("/signup");
            
            boolean result = hasErrorMsg || (stayedOnPage && noSuccess);
            System.out.println("ERROR: " + result + " (hasMsg:" + hasErrorMsg + ", stayed:" + stayedOnPage + ", noSuccess:" + noSuccess + ")");
            return result;
        } catch (Exception e) {
            return true;
        }
    }
    
    public String getErrorMessage() {
        try {
            return driver.findElement(errorMsg).getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getSuccessMessage() {
        try {
            return driver.findElement(successMsg).getText();
        } catch (Exception e) {
            return "";
        }
    }
}
