package easy.shop.dtos;

import easy.shop.entities.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentEntryResponse {

    private Long paymentId;
    private Long contractId;
    private String customerFullName;
    private Integer installmentOrdinal;
    private Double amount;
    private PaymentMethod paymentMethod;
    private LocalDateTime recordedAt;
    private String recordedBy;
}
