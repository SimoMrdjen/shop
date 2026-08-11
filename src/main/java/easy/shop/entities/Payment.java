package easy.shop.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id", referencedColumnName = "id")
    private Installment installment;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * Povezuje sve Payment zapise nastale IZ ISTE korisnikove uplate (npr. kad
     * visak sa jedne rate automatski prekrije i deo naredne) - koristi se da bi
     * priznanica mogla tačno da prikaže raspodelu jedne transakcije po ratama.
     * Null za stare zapise nastale pre uvođenja ovog polja.
     */
    @Column(name = "payment_group_id")
    private String paymentGroupId;

    /**
     * Koliko je iznosilo dugovanje na OVOJ rati neposredno pre ove uplate
     * (installmentAmount - tada_već_uplaćeno). Čuva se eksplicitno da bi
     * raspodela ostala tačna i istorijski, bez obzira na kasnije uplate.
     */
    @Column(name = "remaining_before_payment")
    private Double remainingBeforePayment;
}
