package com.swp.myleague.controller;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.repo.UserRepo;
import com.swp.myleague.security.jwt.JwtUtils;
import com.swp.myleague.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

class LoginControllerTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepo userRepo;
    @Mock JwtUtils jwtUtils;
    @Mock Model model;
    @Mock HttpServletResponse response;
    @InjectMocks AuthController authController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

//    @Test
//    void testLogin_Success() {
//        User user = new User(); user.setIsBan(false);
//        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
//        Authentication auth = mock(Authentication.class);
//        UserDetailsImpl details = mock(UserDetailsImpl.class);
//        when(auth.getPrincipal()).thenReturn(details);
//        when(authenticationManager.authenticate(any())).thenReturn(auth);
//        when(jwtUtils.generateJwtCookie(any())).thenReturn(null);
//        String result = authController.loginUser("user", "pw", response, model);
//        assertTrue(result.contains("redirect"));
//    }
    @Test
    void testLogin_Fail_UserNotFound() {
        when(userRepo.findByUsername("nouser")).thenReturn(Optional.empty());
        String r = authController.loginUser("nouser", "pw", response, model);
        assertEquals("LoginPage", r);
        verify(model).addAttribute(eq("error"), contains("Sai tài khoản hoặc mật khẩu"));
    }
    @Test
    void testLogin_Fail_UserBanned() {
        User user = new User(); user.setIsBan(true);
        when(userRepo.findByUsername("banned")).thenReturn(Optional.of(user));
        String r = authController.loginUser("banned", "pw", response, model);
        assertEquals("LoginPage", r);
        verify(model).addAttribute(eq("error"), contains("bị khóa"));
    }
    @Test
    void testLogin_Fail_AuthException() {
        User user = new User(); user.setIsBan(false);
        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("bad login"));
        String r = authController.loginUser("user", "wrongpw", response, model);
        assertEquals("LoginPage", r);
    }
    @Test
    void testLogin_ErrorMessageInModel() {
        User user = new User(); user.setIsBan(false);
        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("fail"));
        String r = authController.loginUser("user", "failpw", response, model);
        verify(model).addAttribute(eq("error"), anyString());
        assertEquals("LoginPage", r);
    }
    @Test
    void testLogin_MessageOnSuccess() {
        User user = new User(); user.setIsBan(false);
        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl details = mock(UserDetailsImpl.class);
        when(auth.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtCookie(any())).thenReturn(null);
        String result = authController.loginUser("user", "pass", response, model);
        verify(model).addAttribute(eq("message"), contains("thành công"));
    }
    @Test
    void testLogin_ReturnNullModel() {
        User user = new User(); user.setIsBan(false);
        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl details = mock(UserDetailsImpl.class);
        when(auth.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtCookie(any())).thenReturn(null);
        // check code does not throw even when model=null
        String result = authController.loginUser("user", "pass", response, null);
        assertNotNull(result);
    }
    @Test
    void testLogin_Fail_NullResponse() {
        User user = new User(); user.setIsBan(false);
        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl details = mock(UserDetailsImpl.class);
        when(auth.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtCookie(any())).thenReturn(null);
        String result = authController.loginUser("user", "pass", null, model);
        assertNotNull(result);
    }
}
