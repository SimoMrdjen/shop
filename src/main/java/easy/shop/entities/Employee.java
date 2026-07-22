package easy.shop.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to login data
    @OneToOne(optional = false)
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccount user;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}