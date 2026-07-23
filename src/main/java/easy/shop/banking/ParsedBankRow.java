package easy.shop.banking;

import java.time.LocalDate;

/**
 * Jedna transakcija izvučena iz bankovnog izvoda, pre uparivanja sa ugovorom/ratom.
 *
 * @param pozivNaBrojRaw cifre poziva na broj IZ POLJA koje je banka eksplicitno označila
 *                       kao takvo (npr. "PBO: 97 ...", "(97) ..."), NIKAD nagađano iz
 *                       proizvoljnih brojeva u tekstu - pogrešno pogađanje bi moglo
 *                       lažno označiti tuđu ratu kao plaćenu. Null ako polje nije nađeno
 *                       ili ne koristi model 97.
 * @param bankReference  referenca banke - koristi se samo za sprečavanje duplog uvoza
 *                       istog izvoda, ne mora biti globalno jedinstvena sama po sebi.
 */
public record ParsedBankRow(
        String bankName,
        LocalDate transactionDate,
        double amount,
        String description,
        String pozivNaBrojRaw,
        String bankReference
) {
}
