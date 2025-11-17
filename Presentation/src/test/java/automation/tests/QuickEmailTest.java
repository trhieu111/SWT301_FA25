package automation.tests;

import java.util.regex.Pattern;

public class QuickEmailTest {
    public static void main(String[] args) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        Pattern p = Pattern.compile(emailRegex);
        
        String[] testEmails = {
            "",                           // TC2: Should FAIL
            "plainaddress.example.com",   // TC2.1: Should FAIL  
            "user@",                      // TC2.2: Should FAIL
            "user@example",              // TC2.3: Should FAIL
            "a@@b@example.com",          // TC2.4: Should FAIL
            "user@domain.superlongtld",  // TC3: Should FAIL
            "user@domain.c",             // TC3.1: Should FAIL
            "valid@test.com"             // TC1: Should PASS
        };
        
        for (String email : testEmails) {
            boolean matches = p.matcher(email).matches();
            System.out.println("Email: '" + email + "' -> " + (matches ? "VALID" : "INVALID"));
        }
    }
}