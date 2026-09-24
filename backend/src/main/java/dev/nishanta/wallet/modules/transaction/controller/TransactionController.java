package dev.nishanta.wallet.modules.transaction.controller;

import dev.nishanta.wallet.modules.transaction.dto.TransferRequest;
import dev.nishanta.wallet.modules.transaction.dto.TransferResponse;
import dev.nishanta.wallet.modules.transaction.service.TransferService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

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
                                     @CookieValue(value = "deviceId", required = false) String deviceId,
                                     HttpServletRequest httpRequest) {
        return transferService.transfer(request, deviceId, httpRequest.getRemoteAddr());
    }
}
