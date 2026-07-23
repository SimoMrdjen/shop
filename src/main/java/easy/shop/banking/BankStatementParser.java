package easy.shop.banking;

import java.util.List;

interface BankStatementParser {

    /** Naziv banke/formata, za prikaz i za dedupe ključ. */
    String bankName();

    /** Da li ovaj parser prepoznaje dati izvod (na osnovu karakterističnog teksta u zaglavlju). */
    boolean supports(String fullText);

    /** Parsira sve transakcije iz izvoda. */
    List<ParsedBankRow> parse(String fullText);
}
