package easy.shop.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentBreakdownEntryResponse {
    private Long installmentId;
    private Integer installmentOrdinal;
    private Double installmentAmount;
    private Double remainingBefore;
    private Double amountApplied;
    private Double remainingAfter;
}
