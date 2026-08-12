package easy.shop.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Privremena implementacija dok se ne poveže pravi SMS provajder (npr.
 * Infobip) - poruku samo zapisuje u log, ne šalje ništa stvarno. Zamena
 * ovom klasom drugom implementacijom {@link SmsSender} je jedina izmena
 * potrebna da slanje postane pravo.
 */
@Slf4j
@Component
public class NoOpSmsSender implements SmsSender {

    @Override
    public SmsSendResult send(String phoneNumber, String message) {
        log.info("[SMS-SIMULACIJA, provajder nije podešen] Poslao bih SMS na {}: {}", phoneNumber, message);
        return SmsSendResult.ok("SIMULATED-" + System.currentTimeMillis());
    }
}
