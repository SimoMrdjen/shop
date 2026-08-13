package easy.shop.controllers;

import easy.shop.dtos.DebtorCallListEntryResponse;
import easy.shop.dtos.StatisticsOverviewResponse;
import easy.shop.services.CollectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CollectionsController {

    private final CollectionsService collectionsService;

    @GetMapping("/api/admin/debtors/call-list")
    public List<DebtorCallListEntryResponse> callList(
            @RequestParam(defaultValue = "0") int fromDays,
            @RequestParam(defaultValue = "45") int toDays,
            @RequestParam(required = false) Double minAmount) {
        return collectionsService.getDebtorCallList(fromDays, toDays, minAmount);
    }

    @GetMapping("/api/admin/statistics/overview")
    public StatisticsOverviewResponse statisticsOverview(@RequestParam(defaultValue = "9") int inflowPeriods) {
        return collectionsService.getStatisticsOverview(inflowPeriods);
    }
}
