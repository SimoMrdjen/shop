package easy.shop.sms;

/**
 * Apstrakcija za slanje SMS poruka - omogućava da se stvarni provajder
 * (npr. Infobip) uključi kasnije bez izmene ostatka koda. Videti
 * {@link NoOpSmsSender} za trenutnu (probnu) implementaciju.
 */
public interface SmsSender {

    SmsSendResult send(String phoneNumber, String message);
}
