package easy.shop.dtos;

import java.util.List;

public record AuthenticationResponse(
        String accessToken,
        String tokenType,
        String username,
        List<String> roles,
        String firstName,
        String lastName
) {}
