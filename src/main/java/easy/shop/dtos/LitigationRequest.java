package easy.shop.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LitigationRequest {

    @NotNull(message = "Datum je obavezan")
    private LocalDate date;

    private String note;
}
