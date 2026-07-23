package easy.shop.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfirmBankTransactionsRequest {
    private List<Long> transactionIds;
}
