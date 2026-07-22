package easy.shop.controllers;

import easy.shop.dtos.IdCardDataResponse;
import easy.shop.idcard.IdCardReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IdCardController {

    private final IdCardReaderService idCardReaderService;

    @PostMapping("/api/id-card/read")
    public IdCardDataResponse read() {
        return idCardReaderService.readIdCard();
    }
}
