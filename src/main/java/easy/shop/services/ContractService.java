package easy.shop.services;

import easy.shop.dtos.ContractRequest;
import easy.shop.dtos.ContractResponse;
import easy.shop.dtos.DailyPaymentReportResponse;
import easy.shop.dtos.InstallmentResponse;
import easy.shop.dtos.LitigationRequest;
import easy.shop.dtos.PayInstallmentRequest;
import easy.shop.dtos.PaymentBreakdownEntryResponse;
import easy.shop.dtos.PaymentBreakdownResponse;
import easy.shop.dtos.PaymentEntryResponse;
import easy.shop.entities.*;
import easy.shop.exceptions.BadRequestException;
import easy.shop.repositories.CustomerRepository;
import easy.shop.repositories.InstallmentRepository;
import easy.shop.repositories.PaymentRepository;
import easy.shop.repositories.PurchaseContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final PurchaseContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ContractResponse create(ContractRequest req) {
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new BadRequestException("Kupac nije pronađen: " + req.getCustomerId()));

        if (req.getParticipation() >= req.getContractAmount()) {
            throw new BadRequestException("Učešće mora biti manje od iznosa ugovora");
        }

        double financeAmount = req.getContractAmount() - req.getParticipation();
        double installmentAmount = Math.round((financeAmount / req.getNumberOfInstallments()) * 100.0) / 100.0;

        PurchaseContract contract = PurchaseContract.builder()
                .customer(customer)
                .contractAmount(req.getContractAmount())
                .participation(req.getParticipation())
                .contractDate(req.getContractDate())
                .numberOfInstallments(req.getNumberOfInstallments())
                .build();

        List<Installment> installments = new ArrayList<>();
        for (int i = 1; i <= req.getNumberOfInstallments(); i++) {
            installments.add(Installment.builder()
                    .purchaseContract(contract)
                    .installmentOrdinal(i)
                    .installmentAmount(installmentAmount)
                    .maturityDate(req.getContractDate().plusMonths(i))
                    .status(InstallmentStatus.PENDING)
                    .build());
        }
        contract.setInstallments(installments);

        PurchaseContract saved = contractRepository.save(contract);
        return toResponse(saved, saved.getInstallments());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getAll() {
        return contractRepository.findAllWithCustomer().stream()
                .map(c -> toResponse(c, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(Long id) {
        PurchaseContract contract = contractRepository.findByIdWithInstallments(id)
                .orElseThrow(() -> new BadRequestException("Ugovor nije pronađen: " + id));
        Map<Long, String> latestGroupByInstallment = loadLatestPaymentGroupIds(contract.getInstallments());
        return toResponse(contract, contract.getInstallments(), latestGroupByInstallment);
    }

    /** Za svaku ratu nalazi ID grupe njene POSLEDNJE (po vremenu) uplate. */
    private Map<Long, String> loadLatestPaymentGroupIds(List<Installment> installments) {
        List<Long> ids = installments.stream().map(Installment::getId).toList();
        if (ids.isEmpty()) return Map.of();

        Map<Long, String> latestGroupId = new HashMap<>();
        Map<Long, java.time.LocalDateTime> latestTime = new HashMap<>();
        for (Payment p : paymentRepository.findByInstallmentIdIn(ids)) {
            Long instId = p.getInstallment().getId();
            java.time.LocalDateTime createdAt = p.getCreatedAt();
            if (createdAt != null && (!latestTime.containsKey(instId) || createdAt.isAfter(latestTime.get(instId)))) {
                latestTime.put(instId, createdAt);
                latestGroupId.put(instId, p.getPaymentGroupId());
            }
        }
        return latestGroupId;
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getByCustomer(Long customerId) {
        return contractRepository.findByCustomerId(customerId).stream()
                .map(c -> toResponse(c, List.of()))
                .toList();
    }

    @Transactional
    public InstallmentResponse payInstallment(Long installmentId, PayInstallmentRequest req) {
        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new BadRequestException("Rata nije pronađena: " + installmentId));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new BadRequestException("Rata je već plaćena");
        }

        String paymentGroupId = UUID.randomUUID().toString();
        applyPayment(installment, req.getPaidAmount(), req.getPaymentDate(), req.getPaymentMethod(), paymentGroupId);

        return toInstallmentResponse(installment);
    }

    /**
     * Primenjuje iznos uplate na ratu; ako iznos premašuje dugovanje na ovoj rati,
     * višak se rekurzivno prenosi na narednu ratu (ili dalje, ako je i ona već
     * plaćena). Za svaku ratu na koju je stvarno primenjen deo ove uplate čuva se
     * poseban Payment zapis (sa istim paymentGroupId za sve rate iz ISTE uplate),
     * uz tačno dugovanje pre uplate - da bi priznanica kasnije mogla verno da
     * prikaže raspodelu, nezavisno od eventualnih kasnijih uplata.
     */
    private void applyPayment(Installment installment, double amountToApply, LocalDate paymentDate,
                               PaymentMethod paymentMethod, String paymentGroupId) {
        if (installment.getStatus() == InstallmentStatus.PAID) {
            // Rata je vec u potpunosti placena (npr. uplacena van reda) - visak ide dalje.
            if (amountToApply > 0) {
                installmentRepository.findByPurchaseContractIdAndInstallmentOrdinal(
                        installment.getPurchaseContract().getId(), installment.getInstallmentOrdinal() + 1
                ).ifPresent(next -> applyPayment(next, amountToApply, paymentDate, paymentMethod, paymentGroupId));
            }
            return;
        }

        double alreadyPaid = installment.getPaidAmount() != null ? installment.getPaidAmount() : 0.0;
        double remainingBefore = Math.round((installment.getInstallmentAmount() - alreadyPaid) * 100.0) / 100.0;
        double totalPaid = Math.round((alreadyPaid + amountToApply) * 100.0) / 100.0;
        double excess = Math.round((totalPaid - installment.getInstallmentAmount()) * 100.0) / 100.0;
        double appliedToThis = excess >= 0 ? remainingBefore : amountToApply;

        installment.setPaymentDate(paymentDate);
        installment.setPaymentMethod(paymentMethod);

        if (excess >= 0) {
            installment.setPaidAmount(installment.getInstallmentAmount());
            installment.setStatus(InstallmentStatus.PAID);
        } else {
            installment.setPaidAmount(totalPaid);
            installment.setStatus(InstallmentStatus.PARTIAL);
        }
        installmentRepository.save(installment);

        if (appliedToThis > 0) {
            paymentRepository.save(Payment.builder()
                    .installment(installment)
                    .amount(appliedToThis)
                    .paymentDate(paymentDate)
                    .paymentMethod(paymentMethod)
                    .paymentGroupId(paymentGroupId)
                    .remainingBeforePayment(remainingBefore)
                    .build());
        }

        if (excess > 0) {
            installmentRepository.findByPurchaseContractIdAndInstallmentOrdinal(
                    installment.getPurchaseContract().getId(), installment.getInstallmentOrdinal() + 1
            ).ifPresent(next -> applyPayment(next, excess, paymentDate, paymentMethod, paymentGroupId));
        }
    }

    @Transactional(readOnly = true)
    public List<InstallmentResponse> getOverdue() {
        return installmentRepository.findOverdue(LocalDate.now()).stream()
                .map(this::toInstallmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstallmentResponse> getUnpaidByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BadRequestException("Kupac nije pronađen: " + customerId);
        }
        return installmentRepository.findUnpaidByCustomer(customerId).stream()
                .map(this::toInstallmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DailyPaymentReportResponse getDailyPaymentReport(LocalDate date) {
        List<Payment> payments = paymentRepository.findByPaymentDate(date);

        Map<PaymentMethod, Double> totalsByMethod = new EnumMap<>(PaymentMethod.class);
        double grandTotal = 0.0;

        List<PaymentEntryResponse> entries = new ArrayList<>();
        for (Payment p : payments) {
            totalsByMethod.merge(p.getPaymentMethod(), p.getAmount(), Double::sum);
            grandTotal += p.getAmount();

            Installment installment = p.getInstallment();
            PurchaseContract contract = installment.getPurchaseContract();
            Customer customer = contract.getCustomer();

            entries.add(PaymentEntryResponse.builder()
                    .paymentId(p.getId())
                    .contractId(contract.getId())
                    .customerFullName(customer.getFirstName() + " " + customer.getLastName())
                    .installmentOrdinal(installment.getInstallmentOrdinal())
                    .amount(p.getAmount())
                    .paymentMethod(p.getPaymentMethod())
                    .recordedAt(p.getCreatedAt())
                    .recordedBy(p.getCreatedBy())
                    .build());
        }

        return DailyPaymentReportResponse.builder()
                .date(date)
                .grandTotal(Math.round(grandTotal * 100.0) / 100.0)
                .totalsByMethod(totalsByMethod)
                .payments(entries)
                .build();
    }

    // --- Utuženje ---

    @Transactional
    public ContractResponse markSentToLitigation(Long contractId, LitigationRequest req) {
        PurchaseContract contract = contractRepository.findByIdWithInstallments(contractId)
                .orElseThrow(() -> new BadRequestException("Ugovor nije pronađen: " + contractId));
        contract.setSentToLitigation(true);
        contract.setLitigationDate(req.getDate());
        contract.setLitigationNote(req.getNote());
        return toResponse(contractRepository.save(contract), contract.getInstallments());
    }

    @Transactional
    public ContractResponse unmarkLitigation(Long contractId) {
        PurchaseContract contract = contractRepository.findByIdWithInstallments(contractId)
                .orElseThrow(() -> new BadRequestException("Ugovor nije pronađen: " + contractId));
        contract.setSentToLitigation(false);
        contract.setLitigationDate(null);
        contract.setLitigationNote(null);
        return toResponse(contractRepository.save(contract), contract.getInstallments());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsInLitigation() {
        return contractRepository.findAllInLitigation().stream()
                .map(c -> toResponse(c, c.getInstallments()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentBreakdownResponse getPaymentBreakdown(String groupId) {
        List<Payment> payments = paymentRepository.findByPaymentGroupId(groupId);
        if (payments.isEmpty()) {
            throw new BadRequestException("Uplata nije pronađena: " + groupId);
        }

        List<PaymentBreakdownEntryResponse> entries = payments.stream().map(p -> {
            Installment inst = p.getInstallment();
            double remainingBefore = p.getRemainingBeforePayment() != null ? p.getRemainingBeforePayment() : p.getAmount();
            double amountApplied = p.getAmount();
            double remainingAfter = Math.round((remainingBefore - amountApplied) * 100.0) / 100.0;

            return PaymentBreakdownEntryResponse.builder()
                    .installmentId(inst.getId())
                    .installmentOrdinal(inst.getInstallmentOrdinal())
                    .installmentAmount(inst.getInstallmentAmount())
                    .remainingBefore(remainingBefore)
                    .amountApplied(amountApplied)
                    .remainingAfter(Math.max(0, remainingAfter))
                    .build();
        }).toList();

        double totalPaid = Math.round(entries.stream().mapToDouble(PaymentBreakdownEntryResponse::getAmountApplied).sum() * 100.0) / 100.0;
        Payment first = payments.get(0);

        return PaymentBreakdownResponse.builder()
                .contractId(first.getInstallment().getPurchaseContract().getId())
                .paymentDate(first.getPaymentDate())
                .paymentMethod(first.getPaymentMethod())
                .totalPaid(totalPaid)
                .entries(entries)
                .build();
    }

    // --- mappers ---

    private ContractResponse toResponse(PurchaseContract c, List<Installment> installments) {
        return toResponse(c, installments, Map.of());
    }

    private ContractResponse toResponse(PurchaseContract c, List<Installment> installments, Map<Long, String> lastGroupIdByInstallment) {
        double financeAmount = c.getContractAmount() - c.getParticipation();
        double installmentAmount = c.getNumberOfInstallments() > 0
                ? Math.round((financeAmount / c.getNumberOfInstallments()) * 100.0) / 100.0
                : 0;

        return ContractResponse.builder()
                .id(c.getId())
                .customerId(c.getCustomer().getId())
                .customerFullName(c.getCustomer().getFirstName() + " " + c.getCustomer().getLastName())
                .contractAmount(c.getContractAmount())
                .participation(c.getParticipation())
                .financeAmount(financeAmount)
                .contractDate(c.getContractDate())
                .numberOfInstallments(c.getNumberOfInstallments())
                .installmentAmount(installmentAmount)
                .sentToLitigation(c.isSentToLitigation())
                .litigationDate(c.getLitigationDate())
                .litigationNote(c.getLitigationNote())
                .installments(installments.stream()
                        .map(i -> toInstallmentResponse(i, lastGroupIdByInstallment.get(i.getId())))
                        .toList())
                .build();
    }

    private InstallmentResponse toInstallmentResponse(Installment i) {
        return toInstallmentResponse(i, null);
    }

    private InstallmentResponse toInstallmentResponse(Installment i, String lastPaymentGroupId) {
        return InstallmentResponse.builder()
                .id(i.getId())
                .contractId(i.getPurchaseContract().getId())
                .installmentOrdinal(i.getInstallmentOrdinal())
                .installmentAmount(i.getInstallmentAmount())
                .maturityDate(i.getMaturityDate())
                .status(i.getStatus())
                .paidAmount(i.getPaidAmount())
                .paymentDate(i.getPaymentDate())
                .paymentMethod(i.getPaymentMethod())
                .lastPaymentGroupId(lastPaymentGroupId)
                .build();
    }
}
