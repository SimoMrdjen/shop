package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class PeriodStatResponse {
    private String label;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long count;
    private double amount;
}
