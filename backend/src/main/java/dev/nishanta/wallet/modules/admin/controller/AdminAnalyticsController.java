package dev.nishanta.wallet.modules.admin.controller;

import dev.nishanta.wallet.modules.admin.dto.AnalyticsResponse;
import dev.nishanta.wallet.modules.admin.service.AdminAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping
    public AnalyticsResponse getAnalytics() {
        return adminAnalyticsService.getDashboardStats();
    }
}
