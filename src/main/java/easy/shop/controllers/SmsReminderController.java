package easy.shop.controllers;

import easy.shop.dtos.SmsReminderLogResponse;
import easy.shop.dtos.SmsReminderRuleRequest;
import easy.shop.dtos.SmsReminderRuleResponse;
import easy.shop.services.SmsReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/sms-rules")
@RequiredArgsConstructor
public class SmsReminderController {

    private final SmsReminderService smsReminderService;

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
}
