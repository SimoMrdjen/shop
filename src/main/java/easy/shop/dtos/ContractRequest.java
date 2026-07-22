package easy.shop.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ContractRequest {

    @NotNull(message = "ID kupca je obavezan")
    private Long customerId;

    @NotNull(message = "Iznos ugovora je obavezan")
    @Positive(message = "Iznos ugovora mora biti pozitivan")
    private Double contractAmount;

    @NotNull(message = "Ucešce je obavezno")
    @Min(value = 0, message = "Ucesce ne moze biti negativno")
    private Double participation;

    @NotNull(message = "Datum ugovora je obavezan")
    private LocalDate contractDate;

    @NotNull(message = "Broj rata je obavezan")
    @Min(value = 1, message = "Minimalan broj rata je 1")
    private Integer numberOfInstallments;
}
