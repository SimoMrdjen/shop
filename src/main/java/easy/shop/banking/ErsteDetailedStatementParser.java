package easy.shop.banking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser za Erste Bank izvod tipa "Izvod tekučeg računa" (detaljni prikaz).
 *
 * NAPOMENA: nijedan od dosad viđenih primeraka ovog formata nije sadržao stvarnu
 * uplatu rate sa modelom 97 (obe primer-transakcije imale su "referenca partnera" = 0,
 * tj. bez poziva na broj), pa tačna pozicija polja za poziv na broj kada JE popunjeno
 * nije potvrđena uživo. Pretpostavka ispod (drugi broj u nizu "referenca partnera",
 * neposredno pre FT... reference banke) je najverovatnija na osnovu rasporeda kolona
 * u zaglavlju izvoda - preporučuje se provera na prvom stvarnom primeru sa uplatom.
 */
class ErsteDetailedStatementParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d.M.yyyy");

    private static final Pattern ROW_END = Pattern.compile(
            "(FT[0-9A-Za-z]+-?\\s*\\d*)\\s+=(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");

    private static final Pattern DATE = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4})\\.?");

    private static final Pattern REFERENCA_PARTNERA = Pattern.compile("(\\d+)\\s+(\\d+)\\s*$");

    private static final Pattern SECTION_START = Pattern.compile("Referenca banke");

    @Override
    public String bankName() {
        return "ERSTE_DETAILED";
    }

    @Override
    public boolean supports(String fullText) {
        return fullText.contains("Izvod tekučeg računa") || fullText.contains("Din. Teret");
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
            String bankReference = rowEnd.group(1).replaceAll("\\s+", "");
            double amount = MoneyParsing.parseSerbian(rowEnd.group(2));

            LocalDate date = null;
            Matcher dateMatcher = DATE.matcher(transactionsText.substring(rowEnd.end()));
            if (dateMatcher.find()) {
                date = LocalDate.parse(dateMatcher.group(1), DATE_FMT);
            }

            String pozivNaBrojRaw = null;
            Matcher refMatcher = REFERENCA_PARTNERA.matcher(segment.stripTrailing());
            if (refMatcher.find()) {
                String referencaPartnera = refMatcher.group(2);
                if (!referencaPartnera.equals("0")) {
                    pozivNaBrojRaw = referencaPartnera;
                }
            }

            if (date != null) {
                rows.add(new ParsedBankRow(bankName(), date, amount, segment.trim(), pozivNaBrojRaw, bankReference));
            }

            previousEnd = rowEnd.end();
        }

        return rows;
    }
}
