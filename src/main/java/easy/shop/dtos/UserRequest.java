package easy.shop.dtos;

import easy.shop.entities.Role;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    private String username;
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // customer only
    private String jmbg;
    private String address;
    private String idCardNumber;
    private String issuingAuthority;
}
