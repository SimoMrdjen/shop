package easy.shop.services;

import easy.shop.dtos.AuthenticationRequest;
import easy.shop.dtos.AuthenticationResponse;
import easy.shop.entities.UserAccount;
import easy.shop.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthenticationResponse login(AuthenticationRequest request) {
        String username = Optional.ofNullable(request)
                .map(AuthenticationRequest::username)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String password = Optional.of(request)
                .map(AuthenticationRequest::password)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        Authentication authenticated = Optional.of(
                        new UsernamePasswordAuthenticationToken(username, password))
                .map(authenticationManager::authenticate)
                .filter(Authentication::isAuthenticated)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        UserDetails userDetails = Optional.ofNullable(authenticated.getPrincipal())
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast)
                .orElseGet(() -> userDetailsService.loadUserByUsername(username));

        String firstName = null;
        String lastName = null;
        if (userDetails instanceof UserAccount account) {
            if (account.getEmployee() != null) {
                firstName = account.getEmployee().getFirstName();
                lastName  = account.getEmployee().getLastName();
            } else if (account.getCustomer() != null) {
                firstName = account.getCustomer().getFirstName();
                lastName  = account.getCustomer().getLastName();
            }
        }

        return new AuthenticationResponse(
                jwtUtil.generateToken(userDetails),
                "Bearer",
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                firstName,
                lastName
        );
    }
}
