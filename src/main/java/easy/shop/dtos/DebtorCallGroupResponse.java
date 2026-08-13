package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DebtorCallGroupResponse {
    private Long customerId;
    private String customerFullName;
    private String phoneNumber;
    private double totalRemainingAmount;
    private List<DebtorCallInstallmentResponse> installments;
}
