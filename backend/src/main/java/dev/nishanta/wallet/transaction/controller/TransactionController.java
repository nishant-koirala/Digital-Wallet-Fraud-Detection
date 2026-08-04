package dev.nishanta.wallet.transaction.controller;

import dev.nishanta.wallet.transaction.dto.TransferRequest;
import dev.nishanta.wallet.transaction.dto.TransferResponse;
import dev.nishanta.wallet.transaction.service.TransferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransferService transferService;

    public TransactionController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}
