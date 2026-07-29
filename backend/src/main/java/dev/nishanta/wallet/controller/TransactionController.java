package dev.nishanta.wallet.controller;

import dev.nishanta.wallet.domain.Transaction;
import dev.nishanta.wallet.dto.TransferRequest;
import dev.nishanta.wallet.dto.TransferResponse;
import dev.nishanta.wallet.service.TransferService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request) {
        Transaction result = transferService.transfer(
                request.idempotencyKey(),
                request.fromWalletId(),
                request.toWalletId(),
                request.amount()
        );

        // Map the entity to a DTO here, in the controller — this is the
        // one place that decides what's safe to expose over the API.
        // The service layer still returns the full entity internally,
        // since other server-side code might legitimately need it.
        TransferResponse response = new TransferResponse(
                result.getId(),
                result.getStatus().name(),
                result.getFromWallet().getId(),
                result.getToWallet().getId(),
                result.getAmount(),
                result.getCurrency(),
                result.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}