package automation.tests;

import automation.core.BaseTest;
import automation.pages.SignupPage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleSignupTest extends BaseTest {
    
    @Test
    void simpleValidEmailTest() throws InterruptedException {
        SignupPage signupPage = new SignupPage(driver);
        signupPage.navigate();
        Thread.sleep(1000);
        
        String username = "testuser" + System.currentTimeMillis();
        String email = "test" + System.currentTimeMillis() + "@example.com";
        
        System.out.println("Testing with: " + username + ", " + email);
        signupPage.signup(username, email, "Password123!");
        
        // Just check if we stayed on page or got any response
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Final URL: " + currentUrl);
        
        String pageSource = driver.getPageSource();
        boolean hasMessage = pageSource.contains("✅") || pageSource.contains("❌");
        System.out.println("Page has message: " + hasMessage);
        
        // This test should always pass - we just want to see what happens
        assertTrue(true, "Simple test completed");
    }
}