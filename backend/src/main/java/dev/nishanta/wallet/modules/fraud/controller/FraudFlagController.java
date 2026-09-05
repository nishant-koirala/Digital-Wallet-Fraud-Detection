package dev.nishanta.wallet.modules.fraud.controller;

import dev.nishanta.wallet.modules.fraud.dto.FraudFlagResponse;
import dev.nishanta.wallet.modules.fraud.dto.ReviewRequest;
import dev.nishanta.wallet.modules.fraud.dto.ReviewResponse;
import dev.nishanta.wallet.modules.fraud.service.FraudReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nishanta.wallet.common.constant.ApiRoutes;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiRoutes.FRAUD_FLAG_BASE)
public class FraudFlagController {

    private final FraudReviewService fraudReviewService;

    public FraudFlagController(FraudReviewService fraudReviewService) {
        this.fraudReviewService = fraudReviewService;
    }

    @GetMapping(ApiRoutes.FRAUD_FLAG_PENDING)
    public List<FraudFlagResponse> listPending() {
        return fraudReviewService.listPending();
    }

    @PostMapping(ApiRoutes.FRAUD_FLAG_APPROVE)
    public ReviewResponse approve(@PathVariable UUID transactionId, @RequestBody ReviewRequest request) {
        return fraudReviewService.approve(transactionId, request.adminUserId());
    }

    @PostMapping(ApiRoutes.FRAUD_FLAG_REJECT)
    public ReviewResponse reject(@PathVariable UUID transactionId, @RequestBody ReviewRequest request) {
        return fraudReviewService.reject(transactionId, request.adminUserId());
    }
}
