package easy.shop.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "imported_bank_transaction", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bank_name", "bank_reference", "amount", "transaction_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportedBankTransaction extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_reference")
    private String bankReference;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private Double amount;

    private String description;

    @Column(name = "poziv_na_broj_raw")
    private String pozivNaBrojRaw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankTransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_installment_id")
    private Installment matchedInstallment;
}
