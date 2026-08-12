package dev.nishanta.wallet.transaction.controller;

import dev.nishanta.wallet.constant.ApiConstants;
import dev.nishanta.wallet.transaction.dto.TransferRequest;
import dev.nishanta.wallet.transaction.dto.TransferResponse;
import dev.nishanta.wallet.transaction.service.TransferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.Transaction.BASE)
public class TransactionController {

    private final TransferService transferService;

    public TransactionController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping(ApiConstants.Transaction.TRANSFER)
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}
