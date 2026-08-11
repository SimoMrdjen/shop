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

    /** ID grupe poslednje uplate na ovu ratu - koristi se za tačnu raspodelu na priznanici. */
    private String lastPaymentGroupId;
}
