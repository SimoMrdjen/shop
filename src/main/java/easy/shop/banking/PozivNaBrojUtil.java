package easy.shop.banking;

/**
 * Generisanje i dekodiranje "poziva na broj" (model 97) za rate ugovora.
 * Format: [2 kontrolne cifre][ID ugovora][2 cifre - redni broj rate]
 *
 * Mora ostati identično sintaksno usklađeno sa frontend implementacijom u
 * contract-print.component.ts (metode mod97/pozivNaBroj) - ovo je Java "ogledalo"
 * te logike, korišćeno za automatsko dekodiranje pri učitavanju bankovnog izvoda.
 */
public final class PozivNaBrojUtil {

    private PozivNaBrojUtil() {}

    public record Decoded(long contractId, int installmentOrdinal) {}

    private static int mod97(String digits) {
        int remainder = 0;
        for (int i = 0; i < digits.length(); i++) {
            int digit = digits.charAt(i) - '0';
            remainder = (remainder * 10 + digit) % 97;
        }
        return remainder;
    }

    public static String generate(long contractId, int installmentOrdinal) {
        String base = contractId + String.format("%02d", installmentOrdinal);
        int remainder = mod97(base + "00");
        String control = String.format("%02d", 98 - remainder);
        return control + base;
    }

    /**
     * Pokušava da dekodira poziv na broj nazad u (ID ugovora, redni broj rate).
     * Prihvata sirov tekst - sve ne-cifre se ignorišu pre parsiranja.
     * Vraća null ako string nije validan poziv na broj generisan po ovoj šemi
     * (npr. kontrolna cifra se ne poklapa - znači da je to poziv na broj neke
     * druge, nepovezane transakcije).
     */
    public static Decoded decode(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < 5) return null; // min: 2 kontrolne + 1 cifra ugovora + 2 cifre rate

        String control = digits.substring(0, 2);
        String rest = digits.substring(2);
        String ordinalStr = rest.substring(rest.length() - 2);
        String contractIdStr = rest.substring(0, rest.length() - 2);

        if (contractIdStr.isEmpty() || contractIdStr.length() > 1 && contractIdStr.charAt(0) == '0') {
            return null;
        }

        int remainder = mod97(contractIdStr + ordinalStr + "00");
        String expectedControl = String.format("%02d", 98 - remainder);
        if (!expectedControl.equals(control)) return null;

        try {
            return new Decoded(Long.parseLong(contractIdStr), Integer.parseInt(ordinalStr));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
