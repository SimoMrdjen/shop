package easy.shop.repositories;

import easy.shop.entities.ImportedBankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ImportedBankTransactionRepository extends JpaRepository<ImportedBankTransaction, Long> {

    Optional<ImportedBankTransaction> findByBankNameAndBankReferenceAndAmountAndTransactionDate(
            String bankName, String bankReference, Double amount, LocalDate transactionDate);
}
