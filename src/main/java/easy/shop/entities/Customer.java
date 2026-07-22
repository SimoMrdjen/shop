package easy.shop.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccount user;

    private String lastName;
    private String firstName;

    @Column(unique = true)
    private String jmbg;
    private String address;
    private String idCardNumber;      // brLK
    private String issuingAuthority;  // pu
    private String phoneNumber;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseContract> purchaseContracts = new ArrayList<>();
}
