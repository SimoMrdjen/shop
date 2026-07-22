package easy.shop.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdCardDataResponse {
    private String firstName;
    private String lastName;
    private String jmbg;
    private String address;
    private String idCardNumber;
    private String issuingAuthority;
}
