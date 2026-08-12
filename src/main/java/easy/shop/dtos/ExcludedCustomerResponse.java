package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExcludedCustomerResponse {
    private Long customerId;
    private String fullName;
    private String phoneNumber;
}
