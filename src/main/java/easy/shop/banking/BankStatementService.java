package easy.shop.banking;

import easy.shop.dtos.BankImportRowResponse;
import easy.shop.dtos.PayInstallmentRequest;
import easy.shop.entities.BankTransactionStatus;
import easy.shop.entities.ImportedBankTransaction;
import easy.shop.entities.Installment;
import easy.shop.entities.InstallmentStatus;
import easy.shop.entities.PaymentMethod;
import easy.shop.exceptions.BadRequestException;
import easy.shop.repositories.ImportedBankTransactionRepository;
import easy.shop.repositories.InstallmentRepository;
import easy.shop.services.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankStatementService {

    private final BankStatementParserRegistry parserRegistry;
    private final ImportedBankTransactionRepository importedRepository;
    private final InstallmentRepository installmentRepository;
    private final ContractService contractService;

    @Transactional
    public List<BankImportRowResponse> preview(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Nije moguće pročitati fajl: " + e.getMessage());
        }

        List<ParsedBankRow> parsedRows = parserRegistry.parse(bytes);
        if (parsedRows.isEmpty()) {
            throw new BadRequestException("U izvodu nije prepoznata nijedna transakcija.");
        }

        return parsedRows.stream().map(this::toManagedRow).map(this::toResponse).toList();
    }

    @Transactional
    public List<BankImportRowResponse> confirm(List<Long> transactionIds) {
        List<ImportedBankTransaction> transactions = importedRepository.findAllById(transactionIds);

        for (ImportedBankTransaction tx : transactions) {
            if (tx.getStatus() != BankTransactionStatus.PROPOSED_MATCH) {
                continue; // preskoci - vec potvrdjeno ili nije poklopljeno
            }

            PayInstallmentRequest req = new PayInstallmentRequest();
            req.setPaidAmount(tx.getAmount());
            req.setPaymentDate(tx.getTransactionDate());
            req.setPaymentMethod(PaymentMethod.UPLATA_TR);
            contractService.payInstallment(tx.getMatchedInstallment().getId(), req);

            tx.setStatus(BankTransactionStatus.CONFIRMED);
            importedRepository.save(tx);
        }

        return transactions.stream().map(this::toResponse).toList();
    }

    private ImportedBankTransaction toManagedRow(ParsedBankRow row) {
        Optional<ImportedBankTransaction> existing = importedRepository
                .findByBankNameAndBankReferenceAndAmountAndTransactionDate(
                        row.bankName(), row.bankReference(), row.amount(), row.transactionDate());
        if (existing.isPresent()) {
            return existing.get();
        }

        BankTransactionStatus status;
        Installment matchedInstallment = null;

        var decoded = PozivNaBrojUtil.decode(row.pozivNaBrojRaw());
        if (row.pozivNaBrojRaw() == null) {
            status = BankTransactionStatus.NO_REFERENCE;
        } else if (decoded == null) {
            status = BankTransactionStatus.INVALID_REFERENCE;
        } else {
            Optional<Installment> installmentOpt = installmentRepository
                    .findByPurchaseContractIdAndInstallmentOrdinal(decoded.contractId(), decoded.installmentOrdinal());
            if (installmentOpt.isEmpty()) {
                status = BankTransactionStatus.UNKNOWN_CONTRACT;
            } else if (installmentOpt.get().getStatus() == InstallmentStatus.PAID) {
                status = BankTransactionStatus.ALREADY_PAID;
            } else {
                status = BankTransactionStatus.PROPOSED_MATCH;
                matchedInstallment = installmentOpt.get();
            }
        }

        ImportedBankTransaction entity = ImportedBankTransaction.builder()
                .bankName(row.bankName())
                .bankReference(row.bankReference())
                .transactionDate(row.transactionDate())
                .amount(row.amount())
                .description(row.description())
                .pozivNaBrojRaw(row.pozivNaBrojRaw())
                .status(status)
                .matchedInstallment(matchedInstallment)
                .build();

        return importedRepository.save(entity);
    }

    private BankImportRowResponse toResponse(ImportedBankTransaction tx) {
        Installment installment = tx.getMatchedInstallment();
        String customerFullName = installment != null
                ? installment.getPurchaseContract().getCustomer().getFirstName() + " "
                + installment.getPurchaseContract().getCustomer().getLastName()
                : null;

        return BankImportRowResponse.builder()
                .id(tx.getId())
                .bankName(tx.getBankName())
                .transactionDate(tx.getTransactionDate())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .status(tx.getStatus())
                .contractId(installment != null ? installment.getPurchaseContract().getId() : null)
                .installmentOrdinal(installment != null ? installment.getInstallmentOrdinal() : null)
                .customerFullName(customerFullName)
                .payerNameCheck(customerFullName != null
                        ? PayerNameMatcher.check(tx.getDescription(), customerFullName).name()
                        : null)
                .build();
    }
}
