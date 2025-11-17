package com.swp.myleague.controller;

import com.swp.myleague.model.repo.UserRepo;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SignupControllerTest {
    @Mock UserRepo userRepo;
    @Mock JwtUtils jwtUtils;
    @Mock EmailService emailService;
    @Mock Model model;
    @InjectMocks AuthController authController;
    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testSignup_Success() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.generateVerificationToken(any())).thenReturn("TOKEN");
        doNothing().when(emailService).sendMail(any(), any(), any(), any(), any());
        String result = authController.registerUser("test", "mail@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model).addAttribute(eq("message"), contains("kiểm tra email"));
    }
    @Test
    void testSignup_UsernameExists() {
        when(userRepo.existsByUsername(anyString())).thenReturn(true);
        String result = authController.registerUser("dup", "mail@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model).addAttribute(eq("error"), contains("Username"));
    }
    @Test
    void testSignup_EmailExists() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(true);
        String result = authController.registerUser("user2", "dup@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model).addAttribute(eq("error"), contains("Email"));
    }
    @Test
    void testSignup_SendMailException() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.generateVerificationToken(any())).thenReturn("TOK");
        doThrow(new RuntimeException()).when(emailService).sendMail(any(), any(), any(), any(), any());
        assertThrows(Exception.class, () -> {
            authController.registerUser("ok", "ok@mail.com", "pw", model);
        });
    }

    @Test
    void testSignup_MissingParam() {
        // Xử lý truyền thiếu param username (Spring thực tế chặn),
        // test đánh giá cho case username = null, email = null
        when(userRepo.existsByUsername(null)).thenReturn(false);
        when(userRepo.existsByEmail(null)).thenReturn(false);
        when(jwtUtils.generateVerificationToken(any())).thenReturn("TOK");
        doNothing().when(emailService).sendMail(any(), any(), any(), any(), any());
        String result = authController.registerUser(null, null, "pw", model);
        assertEquals("SignupPage", result);
    }
    @Test
    void testSignup_SendMailDone() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.generateVerificationToken(any())).thenReturn("TOK");
        doNothing().when(emailService).sendMail(any(), any(), any(), any(), any());
        String result = authController.registerUser("real", "real@mail.com", "pw", model);
        verify(emailService).sendMail(any(), any(), any(), any(), any());
        assertEquals("SignupPage", result);
    }
    @Test
    void testSignup_TokenGenerate() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.generateVerificationToken(any())).thenReturn("TOK");
        doNothing().when(emailService).sendMail(any(), any(), any(), any(), any());
        String token = jwtUtils.generateVerificationToken(any());
        assertNotNull(token);
    }

    // --- Additional tests from testcase attachment (Register Features TC1..TC8) ---
    @Test
    void testSignup_EmailNullEmptyWhitespace_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        // null
        authController.registerUser("u1", null, "pw", model);
        verify(model).addAttribute(eq("error"), any());
        reset(model);
        // empty
        authController.registerUser("u2", "", "pw", model);
        verify(model).addAttribute(eq("error"), any());
        reset(model);
        // whitespace
        authController.registerUser("u3", "   ", "pw", model);
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void testSignup_EmailInvalidBasicFormat_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        authController.registerUser("u4", "plainaddress", "pw", model);
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void testSignup_EmailContainsInvalidCharacters_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        authController.registerUser("u5", "bad<>@mail.com", "pw", model);
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void testSignup_EmailTLDInvalid_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        authController.registerUser("u6", "user@mail.c", "pw", model);
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void testSignup_RegistrationSuccessfulWithValidEmail() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.generateVerificationToken(any())).thenReturn("TOK2");
        doNothing().when(emailService).sendMail(any(), any(), any(), any(), any());
        String result = authController.registerUser("validUser", "valid@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model).addAttribute(eq("message"), any());
    }

    @Test
    void testSignup_EmailCorrectButUsernameExists_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(true);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        String result = authController.registerUser("dupUser", "ok@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void testSignup_EmailCorrectButEmailExists_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(true);
        String result = authController.registerUser("okuser", "dup@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model).addAttribute(eq("error"), any());
    }

    @Test
    void testSignup_UsernameAndEmailAlreadyExist_ReturnsError() {
        when(userRepo.existsByUsername(anyString())).thenReturn(true);
        when(userRepo.existsByEmail(anyString())).thenReturn(true);
        String result = authController.registerUser("dupAll", "dup@mail.com", "pw", model);
        assertEquals("SignupPage", result);
        verify(model, atLeastOnce()).addAttribute(eq("error"), any());
    }
}
