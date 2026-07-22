package easy.shop.idcard;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * JNA veza ka CelikApi.dll (MUP RS - Čitač elektronske lične karte, API v1.4.2).
 *
 * DLL je upakovan u resources/native/CelikApi.dll (radi bez zasebne instalacije "Čitač"
 * aplikacije na ciljnom računaru); ako iz nekog razloga nije dostupan u resursima, koristi
 * se instalirana verzija iz "C:\Program Files\MUP RS\Celik\CelikApi.dll".
 *
 * Potpisi funkcija i strukture su preuzeti direktno iz CelikApi.h (verzija 1.4.2, 64-bit).
 * Napomena iz zaglavlja: char nizovi u strukturama NEMAJU terminalnu nulu na kraju -
 * stvarna dužina teksta je u pratećem *Size polju, a sadržaj je u UTF-8 formatu.
 */
public interface CelikApiLibrary extends Library {

    String FALLBACK_DLL_PATH = "C:\\Program Files\\MUP RS\\Celik\\CelikApi.dll";

    CelikApiLibrary INSTANCE = Native.load(resolveDllPath(), CelikApiLibrary.class);

    private static String resolveDllPath() {
        try (InputStream in = CelikApiLibrary.class.getResourceAsStream("/native/CelikApi.dll")) {
            if (in != null) {
                File tempFile = File.createTempFile("CelikApi", ".dll");
                tempFile.deleteOnExit();
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return tempFile.getAbsolutePath();
            }
        } catch (IOException ignored) {
            // pada na fallback ispod
        }
        return FALLBACK_DLL_PATH;
    }

    int EID_OK = 0;

    // Verzija apija - jedina trenutno ispravna vrednost po dokumentaciji
    int EID_API_VERSION = 4;

    /** Poziva se jednom, na početku rada sa apijem. */
    int EidStartup(int nApiVersion);

    /** Poziva se jednom, na kraju rada sa apijem. */
    int EidCleanup();

    /**
     * Otvara sesiju sa ličnom kartom u čitaču.
     * @param szReader ime čitača pametnih kartica (prazan string "" bira podrazumevani/prvi dostupan)
     * @param pnCardType izlazni parametar - tip dokumenta (EID_CARD_ID2008=1, ID2014=2, IF2020=3, RP2024=4)
     */
    int EidBeginRead(String szReader, IntByReference pnCardType);

    /** Zatvara sesiju sa ličnom kartom, obavezno posle EidBeginRead. */
    int EidEndRead();

    int EidReadDocumentData(EidDocumentData pData);

    int EidReadFixedPersonalData(EidFixedPersonalData pData);

    int EidReadVariablePersonalData(EidVariablePersonalData pData);

    @Structure.FieldOrder({
            "docRegNo", "docRegNoSize",
            "documentType", "documentTypeSize",
            "issuingDate", "issuingDateSize",
            "expiryDate", "expiryDateSize",
            "issuingAuthority", "issuingAuthoritySize",
            "documentSerialNumber", "documentSerialNumberSize",
            "chipSerialNumber", "chipSerialNumberSize",
            "documentName", "documentNameSize",
    })
    class EidDocumentData extends Structure {
        public byte[] docRegNo = new byte[9];
        public int docRegNoSize;
        public byte[] documentType = new byte[2];
        public int documentTypeSize;
        public byte[] issuingDate = new byte[10];
        public int issuingDateSize;
        public byte[] expiryDate = new byte[10];
        public int expiryDateSize;
        public byte[] issuingAuthority = new byte[100];
        public int issuingAuthoritySize;
        public byte[] documentSerialNumber = new byte[10];
        public int documentSerialNumberSize;
        public byte[] chipSerialNumber = new byte[14];
        public int chipSerialNumberSize;
        public byte[] documentName = new byte[100];
        public int documentNameSize;
    }

    @Structure.FieldOrder({
            "personalNumber", "personalNumberSize",
            "surname", "surnameSize",
            "givenName", "givenNameSize",
            "parentGivenName", "parentGivenNameSize",
            "sex", "sexSize",
            "placeOfBirth", "placeOfBirthSize",
            "stateOfBirth", "stateOfBirthSize",
            "dateOfBirth", "dateOfBirthSize",
            "communityOfBirth", "communityOfBirthSize",
            "statusOfForeigner", "statusOfForeignerSize",
            "nationalityFull", "nationalityFullSize",
            "purposeOfStay", "purposeOfStaySize",
            "eNote", "eNoteSize",
    })
    class EidFixedPersonalData extends Structure {
        public byte[] personalNumber = new byte[13];
        public int personalNumberSize;
        public byte[] surname = new byte[200];
        public int surnameSize;
        public byte[] givenName = new byte[200];
        public int givenNameSize;
        public byte[] parentGivenName = new byte[200];
        public int parentGivenNameSize;
        public byte[] sex = new byte[2];
        public int sexSize;
        public byte[] placeOfBirth = new byte[200];
        public int placeOfBirthSize;
        public byte[] stateOfBirth = new byte[200];
        public int stateOfBirthSize;
        public byte[] dateOfBirth = new byte[10];
        public int dateOfBirthSize;
        public byte[] communityOfBirth = new byte[200];
        public int communityOfBirthSize;
        public byte[] statusOfForeigner = new byte[200];
        public int statusOfForeignerSize;
        public byte[] nationalityFull = new byte[200];
        public int nationalityFullSize;
        public byte[] purposeOfStay = new byte[200];
        public int purposeOfStaySize;
        public byte[] eNote = new byte[200];
        public int eNoteSize;
    }

    @Structure.FieldOrder({
            "state", "stateSize",
            "community", "communitySize",
            "place", "placeSize",
            "street", "streetSize",
            "houseNumber", "houseNumberSize",
            "houseLetter", "houseLetterSize",
            "entrance", "entranceSize",
            "floor", "floorSize",
            "apartmentNumber", "apartmentNumberSize",
            "addressDate", "addressDateSize",
            "addressLabel", "addressLabelSize",
    })
    class EidVariablePersonalData extends Structure {
        public byte[] state = new byte[100];
        public int stateSize;
        public byte[] community = new byte[200];
        public int communitySize;
        public byte[] place = new byte[200];
        public int placeSize;
        public byte[] street = new byte[200];
        public int streetSize;
        public byte[] houseNumber = new byte[20];
        public int houseNumberSize;
        public byte[] houseLetter = new byte[8];
        public int houseLetterSize;
        public byte[] entrance = new byte[10];
        public int entranceSize;
        public byte[] floor = new byte[6];
        public int floorSize;
        public byte[] apartmentNumber = new byte[12];
        public int apartmentNumberSize;
        public byte[] addressDate = new byte[10];
        public int addressDateSize;
        public byte[] addressLabel = new byte[60];
        public int addressLabelSize;
    }

    /** Pretvara (bafer, dužina) polje strukture u Java String (UTF-8, bez terminalne nule). */
    static String decode(byte[] buffer, int size) {
        if (size <= 0) return "";
        int len = Math.min(size, buffer.length);
        return new String(buffer, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }
}
