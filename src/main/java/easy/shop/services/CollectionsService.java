package easy.shop.services;

import easy.shop.dtos.BucketStatResponse;
import easy.shop.dtos.DebtorCallListEntryResponse;
import easy.shop.dtos.PeriodStatResponse;
import easy.shop.dtos.StatisticsOverviewResponse;
import easy.shop.entities.Customer;
import easy.shop.entities.Installment;
import easy.shop.entities.PurchaseContract;
import easy.shop.repositories.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionsService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private final InstallmentRepository installmentRepository;

    // --- Pregled za pozivanje dužnika ---

    @Transactional(readOnly = true)
    public List<DebtorCallListEntryResponse> getDebtorCallList(int fromDays, int toDays, Double minAmount) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(toDays);
        LocalDate toDate = today.minusDays(fromDays);

        return installmentRepository.findForDebtorCallList(fromDate, toDate).stream()
                .map(i -> toCallListEntry(i, today))
                .filter(entry -> minAmount == null || entry.getRemainingAmount() >= minAmount)
                .toList();
    }

    private DebtorCallListEntryResponse toCallListEntry(Installment installment, LocalDate today) {
        PurchaseContract contract = installment.getPurchaseContract();
        Customer customer = contract.getCustomer();
        double remaining = remainingAmount(installment);
        long daysOverdue = ChronoUnit.DAYS.between(installment.getMaturityDate(), today);

        return DebtorCallListEntryResponse.builder()
                .customerId(customer.getId())
                .customerFullName((nullToEmpty(customer.getFirstName()) + " " + nullToEmpty(customer.getLastName())).trim())
                .phoneNumber(customer.getPhoneNumber())
                .contractId(contract.getId())
                .installmentOrdinal(installment.getInstallmentOrdinal())
                .maturityDate(installment.getMaturityDate())
                .daysOverdue(Math.max(0, daysOverdue))
                .remainingAmount(remaining)
                .build();
    }

    // --- Statistika ---

    @Transactional(readOnly = true)
    public StatisticsOverviewResponse getStatisticsOverview(int inflowPeriods) {
        LocalDate today = LocalDate.now();
        List<Installment> unpaid = installmentRepository.findAllUnpaidExcludingLitigation();

        List<Installment> overdue = unpaid.stream().filter(i -> i.getMaturityDate().isBefore(today)).toList();
        List<Installment> notYetDue = unpaid.stream().filter(i -> !i.getMaturityDate().isBefore(today)).toList();

        List<Installment> litigation = installmentRepository.findAllUnpaidInLitigation();
        Set<Long> litigationContractIds = litigation.stream()
                .map(i -> i.getPurchaseContract().getId())
                .collect(Collectors.toSet());

        return StatisticsOverviewResponse.builder()
                .totalUnpaidCount(unpaid.size())
                .totalUnpaidAmount(round(sumRemaining(unpaid)))
                .overdueCount(overdue.size())
                .overdueAmount(round(sumRemaining(overdue)))
                .overdueBuckets(buildOverdueBuckets(overdue, today))
                .notYetDueCount(notYetDue.size())
                .notYetDueAmount(round(sumRemaining(notYetDue)))
                .litigationContractsCount(litigationContractIds.size())
                .litigationAmount(round(sumRemaining(litigation)))
                .expectedInflow(buildInflowPeriods(notYetDue, today, inflowPeriods))
                .build();
    }

    private List<BucketStatResponse> buildOverdueBuckets(List<Installment> overdue, LocalDate today) {
        List<Installment> bucket1 = new ArrayList<>(); // 1-15 dana
        List<Installment> bucket2 = new ArrayList<>(); // 15-45 dana
        List<Installment> bucket3 = new ArrayList<>(); // 45+ dana

        for (Installment i : overdue) {
            long daysLate = ChronoUnit.DAYS.between(i.getMaturityDate(), today);
            if (daysLate <= 15) bucket1.add(i);
            else if (daysLate <= 45) bucket2.add(i);
            else bucket3.add(i);
        }

        return List.of(
                BucketStatResponse.builder().label("1-15 dana").count(bucket1.size()).amount(round(sumRemaining(bucket1))).build(),
                BucketStatResponse.builder().label("15-45 dana").count(bucket2.size()).amount(round(sumRemaining(bucket2))).build(),
                BucketStatResponse.builder().label("45+ dana").count(bucket3.size()).amount(round(sumRemaining(bucket3))).build()
        );
    }

    /**
     * Deli vreme unapred na periode od po 10 dana ("dekade"), poravnate na
     * kalendarski mesec (1-10, 11-20, 21-kraj meseca) - lakše za planiranje
     * nego proizvoljni 10-dnevni prozori koji ne prate mesečne cikluse.
     */
    private List<PeriodStatResponse> buildInflowPeriods(List<Installment> notYetDue, LocalDate today, int numberOfPeriods) {
        List<PeriodStatResponse> periods = new ArrayList<>();
        LocalDate periodStart = dekadaStart(today);

        for (int i = 0; i < numberOfPeriods; i++) {
            LocalDate periodEnd = dekadaEnd(periodStart);
            LocalDate finalStart = periodStart;
            LocalDate finalEnd = periodEnd;

            List<Installment> inPeriod = notYetDue.stream()
                    .filter(inst -> !inst.getMaturityDate().isBefore(finalStart) && !inst.getMaturityDate().isAfter(finalEnd))
                    .toList();

            periods.add(PeriodStatResponse.builder()
                    .label(periodStart.format(DATE_FORMAT) + " - " + periodEnd.format(DATE_FORMAT))
                    .fromDate(periodStart)
                    .toDate(periodEnd)
                    .count(inPeriod.size())
                    .amount(round(sumRemaining(inPeriod)))
                    .build());

            periodStart = periodEnd.plusDays(1);
        }
        return periods;
    }

    private LocalDate dekadaStart(LocalDate date) {
        int day = date.getDayOfMonth();
        int startDay = day <= 10 ? 1 : day <= 20 ? 11 : 21;
        return date.withDayOfMonth(startDay);
    }

    private LocalDate dekadaEnd(LocalDate periodStart) {
        int startDay = periodStart.getDayOfMonth();
        if (startDay == 1) return periodStart.withDayOfMonth(10);
        if (startDay == 11) return periodStart.withDayOfMonth(20);
        return periodStart.withDayOfMonth(periodStart.lengthOfMonth());
    }

    private double sumRemaining(List<Installment> installments) {
        return installments.stream().mapToDouble(this::remainingAmount).sum();
    }

    private double remainingAmount(Installment installment) {
        double paid = installment.getPaidAmount() != null ? installment.getPaidAmount() : 0.0;
        return installment.getInstallmentAmount() - paid;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
