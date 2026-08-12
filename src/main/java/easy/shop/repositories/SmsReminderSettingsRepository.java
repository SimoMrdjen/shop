package easy.shop.repositories;

import easy.shop.entities.SmsReminderSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsReminderSettingsRepository extends JpaRepository<SmsReminderSettings, Long> {
}
