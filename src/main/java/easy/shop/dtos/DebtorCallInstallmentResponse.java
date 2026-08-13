package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class DebtorCallInstallmentResponse {
    private Long contractId;
    private Integer installmentOrdinal;
    private LocalDate maturityDate;
    private long daysOverdue;
    private double remainingAmount;
}
