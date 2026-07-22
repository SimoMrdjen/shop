package easy.shop.dtos;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class
ContractResponse {

    private Long id;
    private Long customerId;
    private String customerFullName;
    private Double contractAmount;
    private Double participation;
    private Double financeAmount;
    private LocalDate contractDate;
    private Integer numberOfInstallments;
    private Double installmentAmount;
    private List<InstallmentResponse> installments;
}
