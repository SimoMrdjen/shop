package easy.shop.sms;

import lombok.Getter;

@Getter
public class SmsSendResult {

    private final boolean success;
    private final String providerMessageId;
    private final String errorMessage;

    private SmsSendResult(boolean success, String providerMessageId, String errorMessage) {
        this.success = success;
        this.providerMessageId = providerMessageId;
        this.errorMessage = errorMessage;
    }

    public static SmsSendResult ok(String providerMessageId) {
        return new SmsSendResult(true, providerMessageId, null);
    }

    public static SmsSendResult failure(String errorMessage) {
        return new SmsSendResult(false, null, errorMessage);
    }
}
