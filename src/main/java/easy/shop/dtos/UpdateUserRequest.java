package easy.shop.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    // customer fields
    private String jmbg;
    private String address;
    private String idCardNumber;
    private String issuingAuthority;
}
