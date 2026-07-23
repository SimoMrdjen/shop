package easy.shop.banking;

import easy.shop.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BankStatementParserRegistry {

    private final List<BankStatementParser> parsers = List.of(
            new ErsteSummaryStatementParser(),
            new ErsteDetailedStatementParser(),
            new HalkbankStatementParser()
    );

    public List<ParsedBankRow> parse(byte[] pdfBytes) {
        String text;
        try {
            text = PdfTextExtractor.extract(pdfBytes);
        } catch (Exception e) {
            throw new BadRequestException("Nije moguće pročitati PDF fajl: " + e.getMessage());
        }

        BankStatementParser parser = parsers.stream()
                .filter(p -> p.supports(text))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Format izvoda nije prepoznat. Podržani formati: Erste Bank, Halkbank."));

        return parser.parse(text);
    }
}
