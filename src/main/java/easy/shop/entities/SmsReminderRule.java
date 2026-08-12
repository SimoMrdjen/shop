package easy.shop.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "sms_reminder_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsReminderRule extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Broj dana u odnosu na datum dospeća rate: 0 = na dan dospeća,
     * pozitivan broj = toliko dana posle dospeća (kašnjenje).
     */
    @Column(name = "days_offset", nullable = false)
    private Integer daysOffset;

    @Column(name = "message_template", columnDefinition = "TEXT", nullable = false)
    private String messageTemplate;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
