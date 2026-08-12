package easy.shop.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsTestSendRequest {

    @NotBlank(message = "Broj telefona je obavezan")
    private String phoneNumber;

    @NotBlank(message = "Tekst poruke je obavezan")
    private String message;
}
