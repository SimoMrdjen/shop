package easy.shop.controllers;

import easy.shop.dtos.ChangePasswordRequest;
import easy.shop.services.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
@Validated
public class AccountController {

    private final UserAccountService userService;

    public AccountController(UserAccountService userService) {
        this.userService = userService;
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req, Principal principal) {
        userService.changePassword(principal.getName(), req);
        return ResponseEntity.noContent().build();
    }
}