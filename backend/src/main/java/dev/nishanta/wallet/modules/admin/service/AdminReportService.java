package dev.nishanta.wallet.modules.admin.service;

import dev.nishanta.wallet.modules.admin.dto.ReportTransactionDTO;
import dev.nishanta.wallet.modules.admin.dto.ReportUserDTO;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.domain.TransactionStatus;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.user.domain.Role;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AdminReportService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public Page<ReportTransactionDTO> getTransactions(TransactionStatus status, Pageable pageable) {
        Page<Transaction> page = (status != null) 
            ? transactionRepository.findByStatus(status, pageable)
            : transactionRepository.findAll(pageable);
            
        return page.map(tx -> new ReportTransactionDTO(
                tx.getId().toString(),
                tx.getFromWallet() != null ? tx.getFromWallet().getId().toString() : "N/A",
                tx.getToWallet() != null ? tx.getToWallet().getId().toString() : "N/A",
                tx.getAmount(),
                tx.getCurrency(),
                tx.getStatus().name(),
                tx.getStatus() == dev.nishanta.wallet.modules.transaction.domain.TransactionStatus.FLAGGED,
                tx.getCreatedAt()
        ));
    }

    public Page<ReportUserDTO> getUsers(Role role, Pageable pageable) {
        Page<User> page = (role != null) 
            ? userRepository.findByRole(role, pageable)
            : userRepository.findAll(pageable);
            
        return page.map(user -> new ReportUserDTO(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A",
                user.getRole().name(),
                user.getKycStatus() != null ? user.getKycStatus().name() : "N/A",
                user.getCreatedAt()
        ));
    }

    public void generateTransactionsCsv(TransactionStatus status, PrintWriter pw) {
        try {
            CSVPrinter printer = new CSVPrinter(pw, CSVFormat.DEFAULT.withHeader(
                "Transaction ID", "Date", "From Wallet ID", "To Wallet ID", "Amount", "Currency", "Status", "Fraud Flag"
            ));
            
            int page = 0;
            int size = 1000;
            Page<Transaction> txPage;
            do {
                txPage = (status != null) ? transactionRepository.findByStatus(status, PageRequest.of(page, size)) : transactionRepository.findAll(PageRequest.of(page, size));
                for (Transaction tx : txPage.getContent()) {
                    printer.printRecord(
                            tx.getId().toString(),
                            tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "N/A",
                            tx.getFromWallet() != null ? tx.getFromWallet().getId().toString() : "N/A",
                            tx.getToWallet() != null ? tx.getToWallet().getId().toString() : "N/A",
                            tx.getAmount().toString(),
                            tx.getCurrency(),
                            tx.getStatus().name(),
                            tx.getStatus() == dev.nishanta.wallet.modules.transaction.domain.TransactionStatus.FLAGGED ? "YES" : "NO"
                    );
                }
                printer.flush();
                page++;
            } while (txPage.hasNext());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate transactions CSV", e);
        }
    }

    public void generateUsersCsv(Role role, PrintWriter pw) {
        try {
            CSVPrinter printer = new CSVPrinter(pw, CSVFormat.DEFAULT.withHeader(
                "User ID", "Name", "Email", "Phone", "Role", "Registered At"
            ));
            
            int page = 0;
            int size = 1000;
            Page<User> userPage;
            do {
                userPage = (role != null) ? userRepository.findByRole(role, PageRequest.of(page, size)) : userRepository.findAll(PageRequest.of(page, size));
                for (User user : userPage.getContent()) {
                    printer.printRecord(
                            user.getId().toString(),
                            user.getName(),
                            user.getEmail(),
                            user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A",
                            user.getRole().name(),
                            user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A"
                    );
                }
                printer.flush();
                page++;
            } while (userPage.hasNext());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate users CSV", e);
        }
    }
}
