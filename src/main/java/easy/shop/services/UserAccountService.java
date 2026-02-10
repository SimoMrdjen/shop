package easy.shop.services;

import easy.shop.dtos.ChangePasswordRequest;
import easy.shop.exceptions.BadRequestException;
import easy.shop.repositories.UserAccountRepository;
import easy.shop.entities.UserAccount;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class  UserAccountService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }


    public boolean existsByUsername(@NotBlank @Email String userName) {
        return userRepository.findByUsername(userName).isPresent();
    }

    @Transactional
    public void resetPasswordWithUsername(String userName, String newPassword) {
        UserAccount user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Optionally validate password strength here

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}