package dev.nishanta.wallet.modules.transaction.controller;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.transaction.dto.BillPaymentRequest;
import dev.nishanta.wallet.modules.transaction.dto.TransferResponse;
import dev.nishanta.wallet.modules.transaction.service.BillPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.TRANSACTION_BASE)
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping("/pay-bill")
    public ResponseEntity<TransferResponse> payBill(@RequestBody BillPaymentRequest request) {
        return ResponseEntity.ok(billPaymentService.payBill(request));
    }
}
