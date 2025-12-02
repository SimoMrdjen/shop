package easy.shop.bootstrap;

import easy.shop.entities.Role;
import easy.shop.entities.UserAccount;
import easy.shop.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:ChangeThisStrongPassword!}")
    private String adminPassword;

    public DataInitializer(UserAccountRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // create default admin only if username not present
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            UserAccount admin = UserAccount.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin created: " + adminUsername);
        }
    }
}