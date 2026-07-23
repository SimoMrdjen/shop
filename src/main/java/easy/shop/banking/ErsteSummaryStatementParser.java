package easy.shop.banking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser za Erste Bank izvod tipa "Dinarski izvod" / "KRATAK PREGLED PROMENA".
 *
 * Svaki red transakcije se u ekstrahovanom tekstu završava sa
 * "<referenca banke> <iznos>", npr. "FT26161H6W6K 60,00" ili
 * "PBO: 97 2491000000063405444 FT26161H6W6K 19.687,11" - ovaj obrazac
 * (referenca pa iznos na kraju reda) koristimo kao granicu izmedju redova.
 */
class ErsteSummaryStatementParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d.M.yyyy");

    private static final Pattern ROW_END = Pattern.compile(
            "([A-Z]{2}\\d{2}[0-9A-Za-z]{4,12})\\s+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");

    private static final Pattern DATE = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4})\\.");

    private static final Pattern PBO = Pattern.compile("PBO:\\s*97\\s+(\\d+)");

    private static final Pattern SECTION_START = Pattern.compile("PREGLED SVIH VAŠIH TRANSAKCIJA");

    @Override
    public String bankName() {
        return "ERSTE_SUMMARY";
    }

    @Override
    public boolean supports(String fullText) {
        return fullText.contains("KRATAK PREGLED PROMENA");
    }

    @Override
    public List<ParsedBankRow> parse(String fullText) {
        String joined = fullText.lines().map(String::trim).filter(l -> !l.isBlank())
                .reduce((a, b) -> a + " " + b).orElse("");

        Matcher sectionMatcher = SECTION_START.matcher(joined);
        String transactionsText = sectionMatcher.find() ? joined.substring(sectionMatcher.end()) : joined;

        List<ParsedBankRow> rows = new ArrayList<>();
        Matcher rowEnd = ROW_END.matcher(transactionsText);
        int previousEnd = 0;

        while (rowEnd.find()) {
            String segment = transactionsText.substring(previousEnd, rowEnd.start());
            String bankReference = rowEnd.group(1);
            double amount = MoneyParsing.parseSerbian(rowEnd.group(2));

            LocalDate date = null;
            Matcher dateMatcher = DATE.matcher(segment);
            if (dateMatcher.find()) {
                date = LocalDate.parse(dateMatcher.group(1), DATE_FMT);
            }

            String pozivNaBrojRaw = null;
            Matcher pboMatcher = PBO.matcher(segment);
            if (pboMatcher.find()) {
                pozivNaBrojRaw = pboMatcher.group(1);
            }

            if (date != null) {
                rows.add(new ParsedBankRow(bankName(), date, amount, segment.trim(), pozivNaBrojRaw, bankReference));
            }

            previousEnd = rowEnd.end();
        }

        return rows;
    }
}
