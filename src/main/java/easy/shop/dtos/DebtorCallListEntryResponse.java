package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class DebtorCallListEntryResponse {
    private Long customerId;
    private String customerFullName;
    private String phoneNumber;
    private Long contractId;
    private Integer installmentOrdinal;
    private LocalDate maturityDate;
    private long daysOverdue;
    private double remainingAmount;
}
