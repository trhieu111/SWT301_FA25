package student.lab2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setup() {
        service = new AccountService();
    }

    @ParameterizedTest(name = "[{index}] {0}, {1}, {2} => {3}")
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    @DisplayName("Kiểm thử đăng ký tài khoản với dữ liệu từ file")
    void testRegisterAccount(String username, String password, String email, boolean expected) {
        boolean result = service.registerAccount(username, password, email);
        assertEquals(expected, result,
                () -> String.format("Expected %s but got %s for input (%s, %s, %s)",
                        expected, result, username, password, email));
    }
}
