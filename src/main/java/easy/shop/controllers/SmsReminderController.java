package easy.shop.controllers;

import easy.shop.dtos.ExcludedCustomerResponse;
import easy.shop.dtos.SmsReminderLogResponse;
import easy.shop.dtos.SmsReminderRuleRequest;
import easy.shop.dtos.SmsReminderRuleResponse;
import easy.shop.dtos.SmsReminderSettingsRequest;
import easy.shop.dtos.SmsReminderSettingsResponse;
import easy.shop.dtos.SmsTestSendRequest;
import easy.shop.services.SmsReminderService;
import easy.shop.sms.SmsSendResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sms-rules")
@RequiredArgsConstructor
public class SmsReminderController {

    private final SmsReminderService smsReminderService;

    @GetMapping("/status")
    public Map<String, Boolean> status() {
        return Map.of("providerConnected", smsReminderService.isProviderConnected());
    }

    @GetMapping
    public List<SmsReminderRuleResponse> getRules() {
        return smsReminderService.getRules();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SmsReminderRuleResponse create(@Valid @RequestBody SmsReminderRuleRequest request) {
        return smsReminderService.createRule(request);
    }

    @PutMapping("/{id}")
    public SmsReminderRuleResponse update(@PathVariable Long id, @Valid @RequestBody SmsReminderRuleRequest request) {
        return smsReminderService.updateRule(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        smsReminderService.deleteRule(id);
    }

    @GetMapping("/log")
    public List<SmsReminderLogResponse> getLog() {
        return smsReminderService.getRecentLogs();
    }

    @PostMapping("/run-now")
    public void runNow() {
        smsReminderService.processRules(LocalDate.now());
    }

    @PostMapping("/test-send")
    public SmsSendResult testSend(@Valid @RequestBody SmsTestSendRequest request) {
        return smsReminderService.sendTestMessage(request.getPhoneNumber(), request.getMessage());
    }

    @GetMapping("/settings")
    public SmsReminderSettingsResponse getSettings() {
        return smsReminderService.getSettings();
    }

    @PutMapping("/settings")
    public SmsReminderSettingsResponse updateSettings(@RequestBody SmsReminderSettingsRequest request) {
        return smsReminderService.updateSettings(request.isSendingEnabled());
    }

    @GetMapping("/excluded-customers")
    public List<ExcludedCustomerResponse> getExcludedCustomers() {
        return smsReminderService.getExcludedCustomers();
    }

    @PostMapping("/excluded-customers/{customerId}")
    public void excludeCustomer(@PathVariable Long customerId) {
        smsReminderService.excludeCustomer(customerId);
    }

    @DeleteMapping("/excluded-customers/{customerId}")
    public void includeCustomer(@PathVariable Long customerId) {
        smsReminderService.includeCustomer(customerId);
    }

    @PostMapping("/{id}/mark-old-as-notified")
    public Map<String, Integer> markOldAsNotified(@PathVariable Long id, @RequestParam(defaultValue = "45") int olderThanDays) {
        int marked = smsReminderService.markOldInstallmentsAsNotified(id, olderThanDays);
        return Map.of("markedCount", marked);
    }
}
