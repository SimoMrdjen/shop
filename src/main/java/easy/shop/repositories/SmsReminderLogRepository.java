package easy.shop.repositories;

import easy.shop.entities.SmsReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsReminderLogRepository extends JpaRepository<SmsReminderLog, Long> {

    boolean existsByInstallmentIdAndRuleId(Long installmentId, Long ruleId);

    List<SmsReminderLog> findTop200ByOrderBySentAtDesc();
}
