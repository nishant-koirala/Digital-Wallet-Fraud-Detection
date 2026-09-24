package dev.nishanta.wallet.modules.transaction.service;

import dev.nishanta.wallet.modules.audit.service.AuditService;
import dev.nishanta.wallet.modules.transaction.dto.BillPaymentRequest;
import dev.nishanta.wallet.modules.transaction.dto.TransferResponse;
import dev.nishanta.wallet.modules.transaction.dto.TransferRequest;
import dev.nishanta.wallet.modules.wallet.domain.Wallet;
import dev.nishanta.wallet.modules.wallet.service.MintWalletProvider;
import org.springframework.stereotype.Service;

@Service
public class BillPaymentService {

    private final TransferService transferService;
    private final MintWalletProvider mintWalletProvider;
    private final AuditService auditService;

    public BillPaymentService(TransferService transferService,
                              MintWalletProvider mintWalletProvider,
                              AuditService auditService) {
        this.transferService = transferService;
        this.mintWalletProvider = mintWalletProvider;
        this.auditService = auditService;
    }

    public TransferResponse payBill(BillPaymentRequest request) {
        Wallet systemWallet = mintWalletProvider.findOrCreateMintWallet();
        
        // We route the money to the system wallet for demo purposes.
        TransferRequest internalRequest = new TransferRequest(
                request.idempotencyKey(),
                request.fromWalletId(),
                systemWallet.getId(),
                null,
                request.amount(),
                null,
                null,
                null,
                null,
                request.pin()
        );

        TransferResponse response = transferService.transfer(internalRequest, null, null);

        auditService.logAction("BILL_PAYMENT", null, "PAY_BILL", request.customerId(), null, request);

        return response;
    }
}
