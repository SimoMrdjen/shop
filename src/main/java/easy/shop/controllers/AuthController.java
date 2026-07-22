
// File: `src/main/java/easy/shop/controllers/AuthController.java`
package easy.shop.controllers;

import easy.shop.dtos.AuthenticationRequest;
import easy.shop.dtos.AuthenticationResponse;
import easy.shop.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/login", "/loging"})
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
    }
}
