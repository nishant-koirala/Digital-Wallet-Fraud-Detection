package dev.nishanta.wallet.modules.admin.service;

import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.transaction.repository.TransactionRepository;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

@Service
public class AdminReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AdminReportService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public byte[] generateTransactionsCsv() {
        List<Transaction> transactions = transactionRepository.findAll();
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(out))) {
             
            CSVPrinter printer = new CSVPrinter(pw, CSVFormat.DEFAULT.withHeader(
                "Transaction ID", "Date", "From Wallet ID", "To Wallet ID", "Amount", "Currency", "Status", "Fraud Flag"
            ));
            
            for (Transaction tx : transactions) {
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
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate transactions CSV", e);
        }
    }

    public byte[] generateUsersCsv() {
        List<User> users = userRepository.findAll();
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(out))) {
             
            CSVPrinter printer = new CSVPrinter(pw, CSVFormat.DEFAULT.withHeader(
                "User ID", "Name", "Email", "Phone", "Role", "Registered At"
            ));
            
            for (User user : users) {
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
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate users CSV", e);
        }
    }
}
