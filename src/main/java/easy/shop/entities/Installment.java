package easy.shop.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "installment")
//@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class Installment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, cascade = MERGE)
    @JoinColumn(name="purchase_contract_id", referencedColumnName = "id")
    private PurchaseContract purchaseContract;

    @Enumerated(EnumType.STRING)
    @Column(name = "installment_ordinal", nullable = false)
    private Integer installmentOrdinal;

    @Column(name = "installment_amount", nullable = false)
    private Double installmentAmount;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    public Installment(PurchaseContract purchaseContract,
                       Integer installmentOrdinal,
                       Double installmentAmount,
                       LocalDate maturityDate,
                       Double paidAmount,
                       LocalDate paymentDate,
                       PaymentMethod paymentMethod) {
        this.purchaseContract = purchaseContract;
        this.installmentOrdinal = installmentOrdinal;
        this.installmentAmount = installmentAmount;
        this.maturityDate = maturityDate;
        this.paidAmount = paidAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public Installment(PurchaseContract purchaseContract,
                       Integer installmentOrdinal,
                       Double installmentAmount,
                       LocalDate maturityDate
    ) {
        this.purchaseContract = purchaseContract;
        this.installmentOrdinal = installmentOrdinal;
        this.installmentAmount = installmentAmount;
        this.maturityDate = maturityDate;
    }
}
