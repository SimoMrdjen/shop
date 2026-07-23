package easy.shop.banking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser za Halkbank izvod ("IZVOD ...", tabela sa kolonom "(Model) poziv na broj").
 *
 * Svaki red počinje linijom koja sadrži samo redni broj i tačku (npr. "1.", "2.").
 */
class HalkbankStatementParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d.M.yyyy");

    private static final Pattern ROW_MARKER = Pattern.compile("^\\d+\\.$");
    private static final Pattern END_MARKER = Pattern.compile("^Ukupno na računu");

    private static final Pattern DATE = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4})");

    private static final Pattern AMOUNTS = Pattern.compile(
            "(\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s+Obr\\.\\s*naknada\\s+(\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s+(\\d{1,3}(?:,\\d{3})*\\.\\d{2})");

    private static final Pattern MODEL_REFERENCE = Pattern.compile("\\((\\d{2})\\)\\s*(\\S+)");

    private static final Pattern BANK_REFERENCE = Pattern.compile("(\\d{10,14})");

    @Override
    public String bankName() {
        return "HALKBANK";
    }

    @Override
    public boolean supports(String fullText) {
        return fullText.contains("HALKBANK") && fullText.contains("poziv na broj");
    }

    @Override
    public List<ParsedBankRow> parse(String fullText) {
        List<String> lines = fullText.lines().map(String::trim).filter(l -> !l.isBlank()).toList();

        List<ParsedBankRow> rows = new ArrayList<>();
        List<String> currentBlock = null;

        for (String line : lines) {
            if (ROW_MARKER.matcher(line).matches()) {
                if (currentBlock != null) {
                    parseBlock(currentBlock).ifPresent(rows::add);
                }
                currentBlock = new ArrayList<>();
                continue;
            }
            if (END_MARKER.matcher(line).find()) {
                if (currentBlock != null) {
                    parseBlock(currentBlock).ifPresent(rows::add);
                }
                currentBlock = null;
                continue;
            }
            if (currentBlock != null) {
                currentBlock.add(line);
            }
        }
        if (currentBlock != null) {
            parseBlock(currentBlock).ifPresent(rows::add);
        }

        return rows;
    }

    private java.util.Optional<ParsedBankRow> parseBlock(List<String> blockLines) {
        String block = String.join(" ", blockLines);

        Matcher dateMatcher = DATE.matcher(block);
        if (!dateMatcher.find()) return java.util.Optional.empty();
        LocalDate date = LocalDate.parse(dateMatcher.group(1), DATE_FMT);

        Matcher amountsMatcher = AMOUNTS.matcher(block);
        if (!amountsMatcher.find()) return java.util.Optional.empty();
        double zaduzenje = MoneyParsing.parseUs(amountsMatcher.group(1));
        double odobrenje = MoneyParsing.parseUs(amountsMatcher.group(3));

        double amount = odobrenje > 0 ? odobrenje : zaduzenje;
        if (amount <= 0) return java.util.Optional.empty();

        String pozivNaBrojRaw = null;
        Matcher modelMatcher = MODEL_REFERENCE.matcher(block);
        while (modelMatcher.find()) {
            if (modelMatcher.group(1).equals("97")) {
                pozivNaBrojRaw = modelMatcher.group(2);
                break;
            }
        }

        String bankReference = null;
        Matcher refMatcher = BANK_REFERENCE.matcher(block);
        while (refMatcher.find()) {
            bankReference = refMatcher.group(1);
        }

        return java.util.Optional.of(new ParsedBankRow(bankName(), date, amount, block, pozivNaBrojRaw, bankReference));
    }
}
