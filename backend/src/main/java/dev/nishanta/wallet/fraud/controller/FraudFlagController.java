package dev.nishanta.wallet.fraud.controller;

import dev.nishanta.wallet.constant.ApiConstants;
import dev.nishanta.wallet.fraud.dto.FraudFlagResponse;
import dev.nishanta.wallet.fraud.dto.ReviewRequest;
import dev.nishanta.wallet.fraud.dto.ReviewResponse;
import dev.nishanta.wallet.fraud.service.FraudReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FraudFlag.BASE)
public class FraudFlagController {

    private final FraudReviewService fraudReviewService;

    public FraudFlagController(FraudReviewService fraudReviewService) {
        this.fraudReviewService = fraudReviewService;
    }

    @GetMapping(ApiConstants.FraudFlag.PENDING)
    public List<FraudFlagResponse> listPending() {
        return fraudReviewService.listPending();
    }

    @PostMapping("/{transactionId}" + ApiConstants.FraudFlag.APPROVE)
    public ReviewResponse approve(@PathVariable UUID transactionId, @RequestBody ReviewRequest request) {
        return fraudReviewService.approve(transactionId, request.adminUserId());
    }

    @PostMapping("/{transactionId}" + ApiConstants.FraudFlag.REJECT)
    public ReviewResponse reject(@PathVariable UUID transactionId, @RequestBody ReviewRequest request) {
        return fraudReviewService.reject(transactionId, request.adminUserId());
    }
}
