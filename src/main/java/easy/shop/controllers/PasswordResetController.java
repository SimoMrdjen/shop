package easy.shop.controllers;

import easy.shop.dtos.ForgotPasswordRequest;
import easy.shop.dtos.ResetPasswordRequest;
import easy.shop.entities.PasswordResetToken;
import easy.shop.repositories.PasswordResetTokenRepository;
import easy.shop.services.EmailService;
import easy.shop.services.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@Validated
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserAccountService userService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
//
//    public PasswordResetController(PasswordResetTokenRepository tokenRepository,
//                                   EmailService emailService,
//                                   UserAccountService userService) {
//        this.tokenRepository = tokenRepository;
//        this.emailService = emailService;
//        this.userService = userService;
//    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        // optional: verify user exists
        if (!userService.existsByUsername(req.getUserName())) {
            // avoid revealing existence — return 204 anyway
            return ResponseEntity.noContent().build();
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expires = LocalDateTime.now().plusHours(1);
        PasswordResetToken prt = new PasswordResetToken(token, req.getUserName(), expires);
        tokenRepository.save(prt);

        String link = frontendUrl + "/reset-password?token=" + token;
        String body = "You requested a password reset. Click the link to set a new password:\n\n" + link +
                      "\n\nThis link expires in 1 hour.";
        emailService.sendSimpleMessage(req.getUserName(), "Password reset", body);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().build();
        }

        PasswordResetToken token = tokenRepository.findByToken(req.getToken()).orElse(null);
        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().build();
        }

        userService.resetPasswordWithUsername(token.getUserName(), req.getNewPassword());
        tokenRepository.deleteByToken(token.getToken());
        return ResponseEntity.noContent().build();
    }
}
