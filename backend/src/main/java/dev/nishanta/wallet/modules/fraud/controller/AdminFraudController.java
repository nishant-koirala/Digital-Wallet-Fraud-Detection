package dev.nishanta.wallet.modules.fraud.controller;

import dev.nishanta.wallet.common.exception.NotFoundException;
import dev.nishanta.wallet.modules.fraud.domain.FraudFlag;
import dev.nishanta.wallet.modules.fraud.domain.ReviewDecision;
import dev.nishanta.wallet.modules.fraud.dto.FraudActionRequest;
import dev.nishanta.wallet.modules.fraud.repository.FraudFlagRepository;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.ledger.LedgerPostingService;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/fraud-flags")
public class AdminFraudController {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerPostingService ledgerPostingService;

    public AdminFraudController(FraudFlagRepository fraudFlagRepository,
                                TransactionRepository transactionRepository,
                                LedgerPostingService ledgerPostingService) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerPostingService = ledgerPostingService;
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingFlags() {
        return fraudFlagRepository.findByReviewed(false).stream().map(flag -> {
            Map<String, Object> map = new HashMap<>();
            map.put("transactionId", flag.getTransaction().getId());
            map.put("walletId", flag.getTransaction().getFromWallet().getId());
            map.put("flagReason", flag.getRuleTriggered());
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public void approve(@PathVariable UUID id, @RequestBody FraudActionRequest request) {
        FraudFlag flag = fraudFlagRepository.findByTransactionId(id)
                .orElseThrow(() -> new NotFoundException("Fraud flag not found for transaction " + id));
        
        flag.review(null, ReviewDecision.APPROVED); // Mock admin user for now
        fraudFlagRepository.save(flag);

        Transaction tx = flag.getTransaction();
        
        // Actually move the money now that it's approved
        ledgerPostingService.postAndComplete(tx, tx.getFromWallet(), tx.getToWallet());
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public void reject(@PathVariable UUID id, @RequestBody FraudActionRequest request) {
        FraudFlag flag = fraudFlagRepository.findByTransactionId(id)
                .orElseThrow(() -> new NotFoundException("Fraud flag not found for transaction " + id));
        
        flag.review(null, ReviewDecision.REJECTED);
        fraudFlagRepository.save(flag);

        Transaction tx = flag.getTransaction();
        tx.markFailed(); // Mark as failed, money is never moved
        transactionRepository.save(tx);
    }
}
