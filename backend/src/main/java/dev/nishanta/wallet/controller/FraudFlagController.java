package dev.nishanta.wallet.controller;

import dev.nishanta.wallet.domain.FraudFlag;
import dev.nishanta.wallet.domain.Transaction;
import dev.nishanta.wallet.dto.FraudFlagResponse;
import dev.nishanta.wallet.dto.ReviewRequest;
import dev.nishanta.wallet.service.FraudReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/fraud-flags")
public class FraudFlagController {

    private final FraudReviewService fraudReviewService;

    public FraudFlagController(FraudReviewService fraudReviewService) {
        this.fraudReviewService = fraudReviewService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FraudFlagResponse>> listPending() {
        List<FraudFlag> pending = fraudReviewService.listPending();

        List<FraudFlagResponse> response = pending.stream()
                .map(flag -> new FraudFlagResponse(
                        flag.getTransaction().getId(),
                        flag.getRuleTriggered(),
                        flag.getRiskScore(),
                        flag.getTransaction().getStatus().name(),
                        flag.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/approve")
    public ResponseEntity<String> approve(@PathVariable UUID transactionId, @RequestBody ReviewRequest request) {
        Transaction result = fraudReviewService.approve(transactionId, request.adminUserId());
        return ResponseEntity.ok("Transaction " + result.getId() + " approved, status: " + result.getStatus());
    }

    @PostMapping("/{transactionId}/reject")
    public ResponseEntity<String> reject(@PathVariable UUID transactionId, @RequestBody ReviewRequest request) {
        Transaction result = fraudReviewService.reject(transactionId, request.adminUserId());
        return ResponseEntity.ok("Transaction " + result.getId() + " rejected, status: " + result.getStatus());
    }
}