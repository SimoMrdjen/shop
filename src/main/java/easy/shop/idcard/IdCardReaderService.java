package easy.shop.idcard;

import com.sun.jna.ptr.IntByReference;
import easy.shop.dtos.IdCardDataResponse;
import easy.shop.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class IdCardReaderService {

    /**
     * Očitava podatke sa lične karte umetnute u čitač.
     * Ceo životni ciklus jedne sesije čitanja: EidStartup -> EidBeginRead -> čitanje blokova
     * podataka -> EidEndRead -> EidCleanup.
     */
    public IdCardDataResponse readIdCard() {
        CelikApiLibrary api = CelikApiLibrary.INSTANCE;

        int rc = api.EidStartup(CelikApiLibrary.EID_API_VERSION);
        if (rc != CelikApiLibrary.EID_OK) {
            throw new BadRequestException("Čitač lične karte nije dostupan (EidStartup greška " + rc + ")");
        }

        try {
            IntByReference cardType = new IntByReference();
            rc = api.EidBeginRead("", cardType);
            if (rc != CelikApiLibrary.EID_OK) {
                throw new BadRequestException("Lična karta nije prepoznata u čitaču (EidBeginRead greška " + rc + "). Da li je kartica umetnuta?");
            }

            try {
                CelikApiLibrary.EidDocumentData document = new CelikApiLibrary.EidDocumentData();
                rc = api.EidReadDocumentData(document);
                if (rc != CelikApiLibrary.EID_OK) {
                    throw new BadRequestException("Greška pri čitanju podataka o dokumentu (kod " + rc + ")");
                }

                CelikApiLibrary.EidFixedPersonalData fixed = new CelikApiLibrary.EidFixedPersonalData();
                rc = api.EidReadFixedPersonalData(fixed);
                if (rc != CelikApiLibrary.EID_OK) {
                    throw new BadRequestException("Greška pri čitanju ličnih podataka sa kartice (kod " + rc + ")");
                }

                CelikApiLibrary.EidVariablePersonalData variable = new CelikApiLibrary.EidVariablePersonalData();
                rc = api.EidReadVariablePersonalData(variable);
                if (rc != CelikApiLibrary.EID_OK) {
                    throw new BadRequestException("Greška pri čitanju adrese sa kartice (kod " + rc + ")");
                }

                String jmbg = CelikApiLibrary.decode(fixed.personalNumber, fixed.personalNumberSize);
                String lastName = CelikApiLibrary.decode(fixed.surname, fixed.surnameSize);
                String firstName = CelikApiLibrary.decode(fixed.givenName, fixed.givenNameSize);
                String idCardNumber = CelikApiLibrary.decode(document.docRegNo, document.docRegNoSize);
                String issuingAuthority = CelikApiLibrary.decode(document.issuingAuthority, document.issuingAuthoritySize);

                String address = buildAddress(variable);

                return IdCardDataResponse.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .jmbg(jmbg)
                        .address(address)
                        .idCardNumber(idCardNumber)
                        .issuingAuthority(issuingAuthority)
                        .build();
            } finally {
                api.EidEndRead();
            }
        } finally {
            api.EidCleanup();
        }
    }

    private String buildAddress(CelikApiLibrary.EidVariablePersonalData v) {
        String place = CelikApiLibrary.decode(v.place, v.placeSize);
        String community = CelikApiLibrary.decode(v.community, v.communitySize);
        String street = CelikApiLibrary.decode(v.street, v.streetSize);
        String houseNumber = CelikApiLibrary.decode(v.houseNumber, v.houseNumberSize);
        String houseLetter = CelikApiLibrary.decode(v.houseLetter, v.houseLetterSize);
        String entrance = CelikApiLibrary.decode(v.entrance, v.entranceSize);
        String floor = CelikApiLibrary.decode(v.floor, v.floorSize);
        String apartmentNumber = CelikApiLibrary.decode(v.apartmentNumber, v.apartmentNumberSize);

        StringBuilder streetPart = new StringBuilder();
        if (!street.isBlank()) streetPart.append(street);
        if (!houseNumber.isBlank()) streetPart.append(' ').append(houseNumber).append(houseLetter);
        if (!entrance.isBlank()) streetPart.append(", ulaz ").append(entrance);
        if (!floor.isBlank()) streetPart.append(", sprat ").append(floor);
        if (!apartmentNumber.isBlank()) streetPart.append(", stan ").append(apartmentNumber);

        StringBuilder result = new StringBuilder();
        if (!place.isBlank()) result.append(place);
        if (!community.isBlank() && !community.equalsIgnoreCase(place)) {
            if (result.length() > 0) result.append(", ");
            result.append(community);
        }
        if (streetPart.length() > 0) {
            if (result.length() > 0) result.append(", ");
            result.append(streetPart);
        }
        return result.toString();
    }
}
