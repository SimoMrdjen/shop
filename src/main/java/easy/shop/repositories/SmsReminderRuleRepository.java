package easy.shop.repositories;

import easy.shop.entities.SmsReminderRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsReminderRuleRepository extends JpaRepository<SmsReminderRule, Long> {

    List<SmsReminderRule> findByActiveTrue();
}
