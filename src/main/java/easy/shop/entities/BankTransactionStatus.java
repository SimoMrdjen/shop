package easy.shop.entities;

public enum BankTransactionStatus {
    /** Poziv na broj je uspešno dekodiran i odgovara nedospelom/neplaćenom ratom - čeka potvrdu zaposlenog. */
    PROPOSED_MATCH,
    /** Zaposleni je potvrdio - uplata je evidentirana na ratu. */
    CONFIRMED,
    /** Transakcija nema polje za poziv na broj (ili nije model 97). */
    NO_REFERENCE,
    /** Poziv na broj postoji ali ne prolazi kontrolu (nije generisan po našoj šemi). */
    INVALID_REFERENCE,
    /** Poziv na broj je validan, ali ugovor/rata sa tim brojem ne postoji u bazi. */
    UNKNOWN_CONTRACT,
    /** Rata na koju poziv na broj upućuje je već u potpunosti plaćena. */
    ALREADY_PAID
}
