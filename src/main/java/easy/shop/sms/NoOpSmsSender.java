package easy.shop.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aktivna dok "sms.infobip.enabled" nije true - poruku samo zapisuje u log,
 * ne šalje ništa stvarno. Videti {@link InfobipSmsSender} za pravo slanje.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.infobip.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpSmsSender implements SmsSender {

    @Override
    public SmsSendResult send(String phoneNumber, String message) {
        log.info("[SMS-SIMULACIJA, provajder nije podešen] Poslao bih SMS na {}: {}", phoneNumber, message);
        return SmsSendResult.ok("SIMULATED-" + System.currentTimeMillis());
    }
}
