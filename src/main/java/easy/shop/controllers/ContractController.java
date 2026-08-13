package easy.shop.controllers;

import easy.shop.dtos.ContractRequest;
import easy.shop.dtos.ContractResponse;
import easy.shop.dtos.DailyPaymentReportResponse;
import easy.shop.dtos.InstallmentResponse;
import easy.shop.dtos.LitigationRequest;
import easy.shop.dtos.PayInstallmentRequest;
import easy.shop.dtos.PaymentBreakdownResponse;
import easy.shop.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // --- Ugovori ---

    @PostMapping("/api/contracts")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse create(@Valid @RequestBody ContractRequest request) {
        return contractService.create(request);
    }

    @GetMapping("/api/contracts")
    public List<ContractResponse> getAll() {
        return contractService.getAll();
    }

    @GetMapping("/api/contracts/{id}")
    public ContractResponse getById(@PathVariable Long id) {
        return contractService.getById(id);
    }

    @GetMapping("/api/contracts/customer/{customerId}")
    public List<ContractResponse> getByCustomer(@PathVariable Long customerId) {
        return contractService.getByCustomer(customerId);
    }

    // --- Rate ---

    @PutMapping("/api/installments/{id}/pay")
    public InstallmentResponse pay(@PathVariable Long id,
                                   @Valid @RequestBody PayInstallmentRequest request) {
        return contractService.payInstallment(id, request);
    }

    @GetMapping("/api/installments/overdue")
    public List<InstallmentResponse> overdue() {
        return contractService.getOverdue();
    }

    @GetMapping("/api/installments/customer/{customerId}/unpaid")
    public List<InstallmentResponse> unpaidByCustomer(@PathVariable Long customerId) {
        return contractService.getUnpaidByCustomer(customerId);
    }

    @GetMapping("/api/installments/payment-groups/{groupId}")
    public PaymentBreakdownResponse paymentBreakdown(@PathVariable String groupId) {
        return contractService.getPaymentBreakdown(groupId);
    }

    // --- Utuženje (samo admin) ---

    @PostMapping("/api/admin/contracts/{id}/litigation")
    public ContractResponse markLitigation(@PathVariable Long id, @Valid @RequestBody LitigationRequest request) {
        return contractService.markSentToLitigation(id, request);
    }

    @DeleteMapping("/api/admin/contracts/{id}/litigation")
    public ContractResponse unmarkLitigation(@PathVariable Long id) {
        return contractService.unmarkLitigation(id);
    }

    @GetMapping("/api/admin/contracts/litigation")
    public List<ContractResponse> litigationContracts() {
        return contractService.getContractsInLitigation();
    }

    // --- Izveštaji ---

    @GetMapping("/api/installments/report/daily")
    public DailyPaymentReportResponse dailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return contractService.getDailyPaymentReport(date != null ? date : LocalDate.now());
    }
}
