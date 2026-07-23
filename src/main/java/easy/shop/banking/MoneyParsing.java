package easy.shop.banking;

final class MoneyParsing {

    private MoneyParsing() {}

    /** "19.687,11" -> 19687.11 (srpski format: tačka za hiljade, zarez za decimale) */
    static double parseSerbian(String s) {
        return Double.parseDouble(s.replace(".", "").replace(",", "."));
    }

    /** "20,860.00" -> 20860.00 (US format: zarez za hiljade, tačka za decimale) */
    static double parseUs(String s) {
        return Double.parseDouble(s.replace(",", ""));
    }
}
