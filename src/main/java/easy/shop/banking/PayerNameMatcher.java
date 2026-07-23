package easy.shop.banking;

/**
 * Dodatna, nezavisna provera: da li se ime i prezime kupca (iz baze) pojavljuju
 * u sirovom tekstu transakcije sa izvoda (koji sadrži naziv platioca). Ovo je
 * pomoćna provera za zaposlenog - poklapanje po pozivu na broj ostaje glavni
 * kriterijum, a ova provera samo upozorava ako se ime platioca sa izvoda ne
 * poklapa sa kupcem na koga poziv na broj upućuje (npr. greška u prepisivanju
 * poziva na broj, ili kupac platio sa tuđeg računa).
 */
final class PayerNameMatcher {

    private PayerNameMatcher() {}

    enum Result { MATCH, PARTIAL, MISMATCH }

    static Result check(String statementText, String customerFullName) {
        String normalizedText = normalize(statementText);
        String[] nameParts = customerFullName.trim().split("\\s+");

        int found = 0;
        int checkable = 0;
        for (String part : nameParts) {
            if (part.length() < 2) continue; // preskoci inicijale i sl.
            checkable++;
            if (normalizedText.contains(normalize(part))) {
                found++;
            }
        }

        if (checkable == 0) return Result.PARTIAL;
        if (found == checkable) return Result.MATCH;
        if (found > 0) return Result.PARTIAL;
        return Result.MISMATCH;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            switch (c) {
                case 'č', 'ć' -> sb.append('c');
                case 'š' -> sb.append('s');
                case 'ž' -> sb.append('z');
                case 'đ' -> sb.append("dj");
                default -> {
                    if (Character.isLetterOrDigit(c)) sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
