package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SmsReminderSettingsResponse {
    private boolean sendingEnabled;
}
