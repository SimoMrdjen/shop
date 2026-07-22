package easy.shop.services;

import easy.shop.dtos.AuthenticationRequest;
import easy.shop.dtos.AuthenticationResponse;
import easy.shop.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsTokenAndRoles() {
        UserDetails userDetails = User.withUsername("john@shop.com")
                .password("encoded")
                .roles("ADMIN")
                .build();

        UsernamePasswordAuthenticationToken authenticated =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any())).thenReturn(authenticated);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        AuthenticationResponse response = authService.login(
                new AuthenticationRequest("  john@shop.com  ", "secret")
        );

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("john@shop.com", response.username());
        assertEquals(List.of("ROLE_ADMIN"), response.roles());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void loginThrowsWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(new AuthenticationRequest("john@shop.com", "wrong"))
        );
    }

    @Test
    void loginThrowsWhenUsernameIsBlank() {
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(new AuthenticationRequest("   ", "secret"))
        );
    }
}
