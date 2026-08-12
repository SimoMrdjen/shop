package easy.shop.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Jedan (singleton) red sa globalnim prekidačem za SMS podsetnike - da
 * admin može trenutno da pauzira SVE slanje (npr. tokom prelaska sa stare
 * na novu aplikaciju), bez brisanja/gašenja pojedinačnih pravila.
 */
@Entity
@Table(name = "sms_reminder_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsReminderSettings {

    @Id
    private Long id;

    @Column(name = "sending_enabled", nullable = false)
    private boolean sendingEnabled;
}
