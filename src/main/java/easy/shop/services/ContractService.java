package easy.shop.services;

import easy.shop.dtos.ContractRequest;
import easy.shop.dtos.ContractResponse;
import easy.shop.dtos.DailyPaymentReportResponse;
import easy.shop.dtos.InstallmentResponse;
import easy.shop.dtos.PayInstallmentRequest;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return toResponse(contract, contract.getInstallments());
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

        double alreadyPaid = installment.getPaidAmount() != null ? installment.getPaidAmount() : 0.0;
        double totalPaid = Math.round((alreadyPaid + req.getPaidAmount()) * 100.0) / 100.0;
        double excess = Math.round((totalPaid - installment.getInstallmentAmount()) * 100.0) / 100.0;

        installment.setPaymentDate(req.getPaymentDate());
        installment.setPaymentMethod(req.getPaymentMethod());

        if (excess >= 0) {
            installment.setPaidAmount(installment.getInstallmentAmount());
            installment.setStatus(InstallmentStatus.PAID);
            installmentRepository.save(installment);
            if (excess > 0) {
                applyExcess(installment, excess, req.getPaymentDate(), req.getPaymentMethod());
            }
        } else {
            installment.setPaidAmount(totalPaid);
            installment.setStatus(InstallmentStatus.PARTIAL);
            installmentRepository.save(installment);
        }

        paymentRepository.save(Payment.builder()
                .installment(installment)
                .amount(req.getPaidAmount())
                .paymentDate(req.getPaymentDate())
                .paymentMethod(req.getPaymentMethod())
                .build());

        return toInstallmentResponse(installment);
    }

    private void applyExcess(Installment current, double excess, LocalDate paymentDate, PaymentMethod paymentMethod) {
        Long contractId = current.getPurchaseContract().getId();
        int nextOrdinal = current.getInstallmentOrdinal() + 1;

        Optional<Installment> nextOpt = installmentRepository
                .findByPurchaseContractIdAndInstallmentOrdinal(contractId, nextOrdinal);

        if (nextOpt.isEmpty()) return;

        Installment next = nextOpt.get();

        if (next.getStatus() == InstallmentStatus.PAID) {
            applyExcess(next, excess, paymentDate, paymentMethod);
            return;
        }

        double alreadyPaid = next.getPaidAmount() != null ? next.getPaidAmount() : 0.0;
        double total = Math.round((alreadyPaid + excess) * 100.0) / 100.0;
        double newExcess = Math.round((total - next.getInstallmentAmount()) * 100.0) / 100.0;

        next.setPaymentDate(paymentDate);
        next.setPaymentMethod(paymentMethod);

        if (newExcess >= 0) {
            next.setPaidAmount(next.getInstallmentAmount());
            next.setStatus(InstallmentStatus.PAID);
            installmentRepository.save(next);
            if (newExcess > 0) {
                applyExcess(next, newExcess, paymentDate, paymentMethod);
            }
        } else {
            next.setPaidAmount(total);
            next.setStatus(InstallmentStatus.PARTIAL);
            installmentRepository.save(next);
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

    // --- mappers ---

    private ContractResponse toResponse(PurchaseContract c, List<Installment> installments) {
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
                .installments(installments.stream().map(this::toInstallmentResponse).toList())
                .build();
    }

    private InstallmentResponse toInstallmentResponse(Installment i) {
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
                .build();
    }
}
