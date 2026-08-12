package easy.shop.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "sms.infobip.enabled", havingValue = "true")
public class InfobipSmsSender implements SmsSender {

    private final RestClient restClient;
    private final String sender;

    public InfobipSmsSender(
            @Value("${sms.infobip.base-url}") String baseUrl,
            @Value("${sms.infobip.api-key}") String apiKey,
            @Value("${sms.infobip.sender:}") String sender) {
        this.sender = sender;
        this.restClient = RestClient.builder()
                .baseUrl("https://" + baseUrl)
                .defaultHeader("Authorization", "App " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
        log.info("Infobip SMS sender aktivan (base-url={})", baseUrl);
    }

    @Override
    public SmsSendResult send(String phoneNumber, String message) {
        Map<String, Object> messageBody = new LinkedHashMap<>();
        messageBody.put("destinations", List.of(Map.of("to", phoneNumber)));
        if (sender != null && !sender.isBlank()) {
            messageBody.put("from", sender);
        }
        messageBody.put("text", message);

        Map<String, Object> payload = Map.of("messages", List.of(messageBody));

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/sms/2/text/advanced")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            String messageId = extractMessageId(response);
            return SmsSendResult.ok(messageId != null ? messageId : "OK");
        } catch (RestClientResponseException e) {
            log.error("Infobip SMS slanje nije uspelo ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return SmsSendResult.failure("Infobip greška " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Infobip SMS slanje nije uspelo", e);
            return SmsSendResult.failure(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<?, ?> response) {
        try {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
            if (messages != null && !messages.isEmpty()) {
                Object id = messages.get(0).get("messageId");
                return id != null ? id.toString() : null;
            }
        } catch (Exception ignored) {
            // Ne blokira slanje ako odgovor ne moze da se ispartsuje - poruka je vec poslata.
        }
        return null;
    }
}
