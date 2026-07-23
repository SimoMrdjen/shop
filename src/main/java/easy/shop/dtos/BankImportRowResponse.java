package easy.shop.dtos;

import easy.shop.entities.BankTransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BankImportRowResponse {
    private Long id;
    private String bankName;
    private LocalDate transactionDate;
    private Double amount;
    private String description;
    private BankTransactionStatus status;
    private Long contractId;
    private Integer installmentOrdinal;
    private String customerFullName;
}
