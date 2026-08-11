package easy.shop.dtos;

import easy.shop.entities.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PaymentBreakdownResponse {
    private Long contractId;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private Double totalPaid;
    private List<PaymentBreakdownEntryResponse> entries;
}
