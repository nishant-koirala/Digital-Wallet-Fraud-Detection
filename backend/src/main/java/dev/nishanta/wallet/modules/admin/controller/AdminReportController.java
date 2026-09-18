package dev.nishanta.wallet.modules.admin.controller;

import dev.nishanta.wallet.modules.admin.dto.ReportTransactionDTO;
import dev.nishanta.wallet.modules.admin.dto.ReportUserDTO;
import dev.nishanta.wallet.modules.admin.service.AdminReportService;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import dev.nishanta.wallet.modules.user.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<ReportTransactionDTO>> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminReportService.getTransactions(status, PageRequest.of(page, size)));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<ReportUserDTO>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminReportService.getUsers(role, PageRequest.of(page, size)));
    }

    @GetMapping("/transactions/csv")
    public ResponseEntity<byte[]> downloadTransactionsCsv(@RequestParam(required = false) TransactionStatus status) {
        byte[] csvBytes = adminReportService.generateTransactionsCsv(status);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @GetMapping("/users/csv")
    public ResponseEntity<byte[]> downloadUsersCsv(@RequestParam(required = false) Role role) {
        byte[] csvBytes = adminReportService.generateUsersCsv(role);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
