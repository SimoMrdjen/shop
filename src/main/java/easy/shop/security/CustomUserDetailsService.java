// File: src/main/java/easy/shop/security/CustomUserDetailsService.java
            package easy.shop.security;

            import easy.shop.entities.UserAccount;
            import easy.shop.repositories.UserAccountRepository;
            import org.springframework.security.core.userdetails.*;
            import org.springframework.stereotype.Service;

            @Service
            public class CustomUserDetailsService implements UserDetailsService {

                private final UserAccountRepository userRepository;

                public CustomUserDetailsService(UserAccountRepository userRepository) {
                    this.userRepository = userRepository;
                }

                @Override
                public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                    UserAccount user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                    return user;
                }
            }