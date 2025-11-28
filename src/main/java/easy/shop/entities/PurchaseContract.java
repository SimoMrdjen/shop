package easy.shop.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseContract extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column(name = "contract_amount", nullable = false)
    private Double contractAmount;

    @Column(name = "participation", nullable = false)
    private Double participation;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "number_of_installments")
    private Integer numberOfInstallments;

    @JsonIgnore
    @OneToMany(mappedBy = "purchaseContract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Installment> installments = new ArrayList<>();
}