package easy.shop.dtos;

import easy.shop.entities.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DailyPaymentReportResponse {

    private LocalDate date;
    private Double grandTotal;
    private Map<PaymentMethod, Double> totalsByMethod;
    private List<PaymentEntryResponse> payments;
}
