package easy.shop.dtos;

import easy.shop.entities.InstallmentStatus;
import easy.shop.entities.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class InstallmentResponse {

    private Long id;
    private Long contractId;
    private Integer installmentOrdinal;
    private Double installmentAmount;
    private LocalDate maturityDate;
    private InstallmentStatus status;

    private Double paidAmount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
}
