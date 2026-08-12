package easy.shop.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsReminderRuleRequest {

    @NotNull(message = "Broj dana je obavezan")
    private Integer daysOffset;

    @NotBlank(message = "Tekst poruke je obavezan")
    private String messageTemplate;

    private boolean active = true;
}
