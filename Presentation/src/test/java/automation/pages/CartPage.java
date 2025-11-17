package automation.pages;

import automation.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CartPage extends BasePage {
    
    // Locators
    private final By subtotalLocator = By.id("subtotal");
    private final By totalLocator = By.id("total");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigate to cart page
     */
    public void navigate() {
        driver.get("http://localhost:8080/cart");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/cart"));
    }

    /**
     * Add product to cart via direct URL with productId (simulates form POST)
     * @param productId The product ID to add
     * @param quantity The quantity to add
     */
    public void addToCartViaURL(String productId, int quantity) {
        String url = "http://localhost:8080/cart?productId=" + productId + "&productAmount=" + quantity + "&url=/product&size=null";
        driver.get(url);
        // Wait for redirect to complete
        waitForSeconds(2);
    }

    /**
     * Increase product quantity by clicking + button for a specific productId
     * @param productId The product ID
     */
    public void increaseQuantity(String productId) {
        String increaseUrl = "http://localhost:8080/cart/ip?productId=" + productId;
        driver.get(increaseUrl);
        waitForSeconds(2);
    }

    /**
     * Decrease product quantity by clicking - button for a specific productId
     * @param productId The product ID
     */
    public void decreaseQuantity(String productId) {
        String decreaseUrl = "http://localhost:8080/cart/dp?productId=" + productId;
        driver.get(decreaseUrl);
        waitForSeconds(2);
    }

    /**
     * Get the number of items currently in the cart
     * @return Number of cart items
     */
    public int getCartItemCount() {
        navigate();
        try {
            // Count quantity-control divs which only appear for actual cart items
            List<WebElement> quantityControls = driver.findElements(By.cssSelector(".quantity-control"));
            System.out.println("Found " + quantityControls.size() + " items with quantity controls");
            return quantityControls.size();
        } catch (Exception e) {
            System.out.println("Error counting cart items: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get quantity of a specific product in cart
     * @param productId The product ID
     * @return Quantity or 0 if not found
     */
    public int getItemQuantity(String productId) {
        navigate();
        try {
            // Find all links with productId parameter
            List<WebElement> links = driver.findElements(By.cssSelector("a[href*='productId=" + productId + "']"));
            if (links.isEmpty()) {
                System.out.println("Product " + productId + " not found in cart");
                return 0;
            }
            
            // Get the quantity span near the first link
            WebElement link = links.get(0);
            WebElement parentDiv = link.findElement(By.xpath("./ancestor::div[contains(@class, 'quantity-control')]"));
            WebElement qtySpan = parentDiv.findElement(By.tagName("span"));
            String qtyText = qtySpan.getText().trim();
            int qty = Integer.parseInt(qtyText);
            System.out.println("Product " + productId + " has quantity: " + qty);
            return qty;
        } catch (Exception e) {
            System.out.println("Error getting quantity for " + productId + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Check if cart is empty by counting items
     * @return true if cart has no items
     */
    public boolean isCartEmpty() {
        return getCartItemCount() == 0;
    }

    /**
     * Get the total price from the cart summary
     * @return Total price as string (e.g., "120000.00 VND")
     */
    public String getTotal() {
        navigate();
        try {
            WebElement totalElement = driver.findElement(totalLocator);
            return totalElement.getText();
        } catch (Exception e) {
            return "0.00 VND";
        }
    }

    /**
     * Get the subtotal price from the cart summary
     * @return Subtotal price as string
     */
    public String getSubtotal() {
        navigate();
        try {
            WebElement subtotalElement = driver.findElement(subtotalLocator);
            return subtotalElement.getText();
        } catch (Exception e) {
            return "0.00 VND";
        }
    }

    /**
     * Check if cart page displays successfully
     * @return true if on cart/checkout page
     */
    public boolean isCartPageDisplayed() {
        return driver.getCurrentUrl().contains("/cart");
    }

    /**
     * Wait for a number of seconds
     * @param seconds Number of seconds to wait
     */
    private void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
