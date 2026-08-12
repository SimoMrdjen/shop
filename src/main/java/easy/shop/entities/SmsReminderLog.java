package easy.shop.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "sms_reminder_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"installment_id", "rule_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "installment_id")
    private Installment installment;

    // Istorija poslatih poruka mora da preživi brisanje pravila (audit trail) -
    // zato SET NULL umesto podrazumevanog RESTRICT, koje bi sprečilo brisanje
    // bilo kog pravila koje je ikad nešto poslalo.
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "rule_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private SmsReminderRule rule;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SmsReminderStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
