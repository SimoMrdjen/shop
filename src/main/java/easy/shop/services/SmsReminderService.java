package easy.shop.services;

import easy.shop.dtos.ExcludedCustomerResponse;
import easy.shop.dtos.SmsReminderLogResponse;
import easy.shop.dtos.SmsReminderRuleRequest;
import easy.shop.dtos.SmsReminderRuleResponse;
import easy.shop.dtos.SmsReminderSettingsResponse;
import easy.shop.entities.Customer;
import easy.shop.entities.Installment;
import easy.shop.entities.PurchaseContract;
import easy.shop.entities.SmsReminderLog;
import easy.shop.entities.SmsReminderRule;
import easy.shop.entities.SmsReminderSettings;
import easy.shop.entities.SmsReminderStatus;
import easy.shop.exceptions.BadRequestException;
import easy.shop.repositories.CustomerRepository;
import easy.shop.repositories.InstallmentRepository;
import easy.shop.repositories.SmsReminderLogRepository;
import easy.shop.repositories.SmsReminderRuleRepository;
import easy.shop.repositories.SmsReminderSettingsRepository;
import easy.shop.sms.SmsSendResult;
import easy.shop.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Long SETTINGS_ID = 1L;

    private final SmsReminderRuleRepository ruleRepository;
    private final SmsReminderLogRepository logRepository;
    private final InstallmentRepository installmentRepository;
    private final SmsReminderSettingsRepository settingsRepository;
    private final CustomerRepository customerRepository;
    private final SmsSender smsSender;

    @Value("${sms.infobip.enabled:false}")
    private boolean providerConnected;

    public boolean isProviderConnected() {
        return providerConnected;
    }

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

    /**
     * Šalje probnu poruku direktno na zadati broj, bez ikakve veze sa
     * pravilima/ratama - služi samo da se proveri da li je konekcija ka
     * SMS provajderu ispravna, pre nego što se pravila aktiviraju na
     * stvarnim kupcima.
     */
    public SmsSendResult sendTestMessage(String phoneNumber, String message) {
        return smsSender.send(normalizePhoneNumber(phoneNumber), message);
    }

    // --- Globalni prekidač ---

    public SmsReminderSettingsResponse getSettings() {
        return SmsReminderSettingsResponse.builder().sendingEnabled(getOrCreateSettings().isSendingEnabled()).build();
    }

    @Transactional
    public SmsReminderSettingsResponse updateSettings(boolean sendingEnabled) {
        SmsReminderSettings settings = getOrCreateSettings();
        settings.setSendingEnabled(sendingEnabled);
        settingsRepository.save(settings);
        return SmsReminderSettingsResponse.builder().sendingEnabled(sendingEnabled).build();
    }

    private SmsReminderSettings getOrCreateSettings() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> settingsRepository.save(
                        SmsReminderSettings.builder().id(SETTINGS_ID).sendingEnabled(true).build()));
    }

    // --- Isključeni kupci ---

    public List<ExcludedCustomerResponse> getExcludedCustomers() {
        return customerRepository.findBySmsRemindersEnabledFalse().stream()
                .map(c -> ExcludedCustomerResponse.builder()
                        .customerId(c.getId())
                        .fullName((nullToEmpty(c.getFirstName()) + " " + nullToEmpty(c.getLastName())).trim())
                        .phoneNumber(c.getPhoneNumber())
                        .build())
                .toList();
    }

    @Transactional
    public void excludeCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BadRequestException("Kupac nije pronađen: " + customerId));
        customer.setSmsRemindersEnabled(false);
        customerRepository.save(customer);
    }

    @Transactional
    public void includeCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BadRequestException("Kupac nije pronađen: " + customerId));
        customer.setSmsRemindersEnabled(true);
        customerRepository.save(customer);
    }

    // --- Slanje ---

    @Scheduled(cron = "0 0 11 * * *")
    public void dispatchDueReminders() {
        processRules(LocalDate.now());
    }

    @Transactional
    public void processRules(LocalDate today) {
        if (!getOrCreateSettings().isSendingEnabled()) {
            log.info("SMS podsetnici su globalno isključeni - preskačem proveru");
            return;
        }
        List<SmsReminderRule> activeRules = ruleRepository.findByActiveTrue();
        for (SmsReminderRule rule : activeRules) {
            List<Installment> due;
            if (rule.getDaysOffset() == 0) {
                // "Na dan dospeća" - tačan datum, ne svaka neplaćena rata do danas.
                due = installmentRepository.findUnpaidByMaturityDate(today);
            } else {
                // "N dana posle dospeća" - hvata i starije zaostale rate koje nikad
                // nisu dobile OVU poruku (npr. ako je app bila ugašena tog dana, ili
                // je pravilo tek sad dodato/aktivirano) - existsByInstallmentIdAndRuleId
                // ispod i dalje sprečava duplo slanje iste rate za isto pravilo.
                LocalDate cutoffDate = today.minusDays(rule.getDaysOffset());
                due = installmentRepository.findUnpaidWithMaturityDateOnOrBefore(cutoffDate);
            }
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

        if (!customer.isSmsRemindersEnabled()) {
            logRepository.save(logBuilder
                    .status(SmsReminderStatus.SKIPPED)
                    .errorMessage("Kupac je isključen iz SMS podsetnika")
                    .build());
            return;
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            logRepository.save(logBuilder
                    .status(SmsReminderStatus.SKIPPED)
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
