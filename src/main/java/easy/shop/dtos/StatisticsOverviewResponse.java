package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StatisticsOverviewResponse {
    private long totalUnpaidCount;
    private double totalUnpaidAmount;

    private long overdueCount;
    private double overdueAmount;
    private List<BucketStatResponse> overdueBuckets;

    private long notYetDueCount;
    private double notYetDueAmount;

    private long litigationContractsCount;
    private double litigationAmount;

    private List<PeriodStatResponse> expectedInflow;
}
