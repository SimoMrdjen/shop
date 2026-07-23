package easy.shop.controllers;

import easy.shop.banking.BankStatementService;
import easy.shop.dtos.BankImportRowResponse;
import easy.shop.dtos.ConfirmBankTransactionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bank-statements")
@RequiredArgsConstructor
public class BankStatementController {

    private final BankStatementService bankStatementService;

    @PostMapping("/preview")
    public List<BankImportRowResponse> preview(@RequestParam("file") MultipartFile file) {
        return bankStatementService.preview(file);
    }

    @PostMapping("/confirm")
    public List<BankImportRowResponse> confirm(@RequestBody ConfirmBankTransactionsRequest request) {
        return bankStatementService.confirm(request.getTransactionIds());
    }
}
