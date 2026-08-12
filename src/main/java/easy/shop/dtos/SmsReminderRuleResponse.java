package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SmsReminderRuleResponse {
    private Long id;
    private Integer daysOffset;
    private String messageTemplate;
    private boolean active;
}
