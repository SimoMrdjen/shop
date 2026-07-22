package easy.shop.dtos;

import easy.shop.entities.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;
    private String username;
    private Role role;

    private Long profileId;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private String jmbg;
    private String address;
    private String idCardNumber;
    private String issuingAuthority;
}
