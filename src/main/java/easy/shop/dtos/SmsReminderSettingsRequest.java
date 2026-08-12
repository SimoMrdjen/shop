package easy.shop.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsReminderSettingsRequest {
    private boolean sendingEnabled;
}
