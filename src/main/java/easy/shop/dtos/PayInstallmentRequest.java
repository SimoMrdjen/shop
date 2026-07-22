package easy.shop.dtos;

import easy.shop.entities.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PayInstallmentRequest {

    @NotNull(message = "Iznos placanja je obavezan")
    @Positive(message = "Iznos mora biti pozitivan")
    private Double paidAmount;

    @NotNull(message = "Datum placanja je obavezan")
    private LocalDate paymentDate;

    @NotNull(message = "Nacin placanja je obavezan")
    private PaymentMethod paymentMethod;
}
