package easy.shop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SmsReminderLogResponse {
    private Long id;
    private String customerName;
    private String phoneNumber;
    private Long contractId;
    private Integer installmentOrdinal;
    private String message;
    private String status;
    private String errorMessage;
    private LocalDateTime sentAt;
}
