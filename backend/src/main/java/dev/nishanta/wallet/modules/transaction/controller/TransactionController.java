package dev.nishanta.wallet.modules.transaction.controller;

import dev.nishanta.wallet.modules.transaction.dto.TransferRequest;
import dev.nishanta.wallet.modules.transaction.dto.TransferResponse;
import dev.nishanta.wallet.modules.transaction.service.TransferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nishanta.wallet.common.constant.ApiRoutes;

@RestController
@RequestMapping(ApiRoutes.TRANSACTION_BASE)
public class TransactionController {

    private final TransferService transferService;

    public TransactionController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping(ApiRoutes.TRANSACTION_TRANSFER)
    public TransferResponse transfer(@RequestBody TransferRequest request,
                                     @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                     @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        return transferService.transfer(request, deviceId, ipAddress);
    }
}
