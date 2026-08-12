package easy.shop.services;

import easy.shop.dtos.SmsReminderLogResponse;
import easy.shop.dtos.SmsReminderRuleRequest;
import easy.shop.dtos.SmsReminderRuleResponse;
import easy.shop.entities.Customer;
import easy.shop.entities.Installment;
import easy.shop.entities.PurchaseContract;
import easy.shop.entities.SmsReminderLog;
import easy.shop.entities.SmsReminderRule;
import easy.shop.entities.SmsReminderStatus;
import easy.shop.exceptions.BadRequestException;
import easy.shop.repositories.InstallmentRepository;
import easy.shop.repositories.SmsReminderLogRepository;
import easy.shop.repositories.SmsReminderRuleRepository;
import easy.shop.sms.SmsSendResult;
import easy.shop.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsReminderService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private final SmsReminderRuleRepository ruleRepository;
    private final SmsReminderLogRepository logRepository;
    private final InstallmentRepository installmentRepository;
    private final SmsSender smsSender;

    // --- Pravila (admin CRUD) ---

    public List<SmsReminderRuleResponse> getRules() {
        return ruleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public SmsReminderRuleResponse createRule(SmsReminderRuleRequest req) {
        SmsReminderRule rule = SmsReminderRule.builder()
                .daysOffset(req.getDaysOffset())
                .messageTemplate(req.getMessageTemplate())
                .active(req.isActive())
                .build();
        return toResponse(ruleRepository.save(rule));
    }

    @Transactional
    public SmsReminderRuleResponse updateRule(Long id, SmsReminderRuleRequest req) {
        SmsReminderRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pravilo nije pronađeno: " + id));
        rule.setDaysOffset(req.getDaysOffset());
        rule.setMessageTemplate(req.getMessageTemplate());
        rule.setActive(req.isActive());
        return toResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new BadRequestException("Pravilo nije pronađeno: " + id);
        }
        ruleRepository.deleteById(id);
    }

    public List<SmsReminderLogResponse> getRecentLogs() {
        return logRepository.findTop200ByOrderBySentAtDesc().stream().map(this::toLogResponse).toList();
    }

    // --- Slanje ---

    @Scheduled(cron = "0 0 9 * * *")
    public void dispatchDueReminders() {
        processRules(LocalDate.now());
    }

    @Transactional
    public void processRules(LocalDate today) {
        List<SmsReminderRule> activeRules = ruleRepository.findByActiveTrue();
        for (SmsReminderRule rule : activeRules) {
            LocalDate targetMaturityDate = today.minusDays(rule.getDaysOffset());
            List<Installment> due = installmentRepository.findUnpaidByMaturityDate(targetMaturityDate);
            for (Installment installment : due) {
                sendReminderIfNotAlreadySent(installment, rule);
            }
        }
    }

    private void sendReminderIfNotAlreadySent(Installment installment, SmsReminderRule rule) {
        if (logRepository.existsByInstallmentIdAndRuleId(installment.getId(), rule.getId())) {
            return;
        }

        PurchaseContract contract = installment.getPurchaseContract();
        Customer customer = contract.getCustomer();
        String phoneNumber = normalizePhoneNumber(customer.getPhoneNumber());
        String message = renderTemplate(rule.getMessageTemplate(), installment, contract, customer);

        SmsReminderLog.SmsReminderLogBuilder logBuilder = SmsReminderLog.builder()
                .installment(installment)
                .rule(rule)
                .phoneNumber(phoneNumber)
                .message(message)
                .sentAt(LocalDateTime.now());

        if (phoneNumber == null || phoneNumber.isBlank()) {
            logRepository.save(logBuilder
                    .status(SmsReminderStatus.FAILED)
                    .errorMessage("Kupac nema unet broj telefona")
                    .build());
            return;
        }

        try {
            SmsSendResult result = smsSender.send(phoneNumber, message);
            if (result.isSuccess()) {
                logRepository.save(logBuilder.status(SmsReminderStatus.SENT).build());
            } else {
                logRepository.save(logBuilder.status(SmsReminderStatus.FAILED).errorMessage(result.getErrorMessage()).build());
            }
        } catch (Exception e) {
            log.error("Slanje SMS podsetnika nije uspelo za ratu {}", installment.getId(), e);
            logRepository.save(logBuilder.status(SmsReminderStatus.FAILED).errorMessage(e.getMessage()).build());
        }
    }

    private String renderTemplate(String template, Installment installment, PurchaseContract contract, Customer customer) {
        double paid = installment.getPaidAmount() != null ? installment.getPaidAmount() : 0.0;
        double remaining = Math.round((installment.getInstallmentAmount() - paid) * 100.0) / 100.0;

        return template
                .replace("{ime}", nullToEmpty(customer.getFirstName()))
                .replace("{prezime}", nullToEmpty(customer.getLastName()))
                .replace("{iznos}", String.valueOf(remaining))
                .replace("{ugovor}", String.valueOf(contract.getId()))
                .replace("{datum}", installment.getMaturityDate().format(DATE_FORMAT));
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * Svodi broj telefona na međunarodni format bez "+" (npr. 381641234567),
     * kakav SMS provajderi obično očekuju. Kupci trenutno unose broj kao
     * slobodan tekst, pa je ovo najbolji napor, ne stroga validacija.
     */
    private String normalizePhoneNumber(String rawPhoneNumber) {
        if (rawPhoneNumber == null) {
            return null;
        }
        String digits = rawPhoneNumber.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        }
        if (digits.startsWith("0")) {
            digits = "381" + digits.substring(1);
        }
        return digits.isBlank() ? null : digits;
    }

    private SmsReminderRuleResponse toResponse(SmsReminderRule rule) {
        return SmsReminderRuleResponse.builder()
                .id(rule.getId())
                .daysOffset(rule.getDaysOffset())
                .messageTemplate(rule.getMessageTemplate())
                .active(rule.isActive())
                .build();
    }

    private SmsReminderLogResponse toLogResponse(SmsReminderLog logEntry) {
        Installment installment = logEntry.getInstallment();
        PurchaseContract contract = installment != null ? installment.getPurchaseContract() : null;
        Customer customer = contract != null ? contract.getCustomer() : null;
        return SmsReminderLogResponse.builder()
                .id(logEntry.getId())
                .customerName(customer != null ? (nullToEmpty(customer.getFirstName()) + " " + nullToEmpty(customer.getLastName())).trim() : null)
                .phoneNumber(logEntry.getPhoneNumber())
                .contractId(contract != null ? contract.getId() : null)
                .installmentOrdinal(installment != null ? installment.getInstallmentOrdinal() : null)
                .message(logEntry.getMessage())
                .status(logEntry.getStatus().name())
                .errorMessage(logEntry.getErrorMessage())
                .sentAt(logEntry.getSentAt())
                .build();
    }
}
