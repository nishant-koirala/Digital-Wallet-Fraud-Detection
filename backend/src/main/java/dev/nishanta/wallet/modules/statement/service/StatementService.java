package dev.nishanta.wallet.modules.statement.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dev.nishanta.wallet.modules.transaction.domain.Transaction;
import dev.nishanta.wallet.modules.wallet.service.WalletService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private final WalletService walletService;

    public StatementService(WalletService walletService) {
        this.walletService = walletService;
    }

    public byte[] generatePdfStatement(UUID walletId) {
        List<Transaction> transactions = walletService.getTransactions(walletId);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Account Statement", titleFont));
            document.add(new Paragraph("Wallet ID: " + walletId));
            document.add(new Paragraph(" ")); // Spacer

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Date");
            table.addCell("Transaction ID");
            table.addCell("Status");
            table.addCell("Amount");

            for (Transaction tx : transactions) {
                table.addCell(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "N/A");
                table.addCell(tx.getId().toString());
                table.addCell(tx.getStatus().name());
                // Determine if credit or debit
                boolean isOutgoing = tx.getFromWallet().getId().equals(walletId);
                String amountStr = (isOutgoing ? "-" : "+") + tx.getAmount().toString() + " " + tx.getCurrency();
                table.addCell(amountStr);
            }

            document.add(table);
            document.close();
            
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF statement", e);
        }
    }

    public byte[] generateCsvStatement(UUID walletId) {
        List<Transaction> transactions = walletService.getTransactions(walletId);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(out))) {
             
            CSVPrinter printer = new CSVPrinter(pw, CSVFormat.DEFAULT.withHeader("Date", "Transaction ID", "Status", "Amount", "Type"));
            
            for (Transaction tx : transactions) {
                boolean isOutgoing = tx.getFromWallet().getId().equals(walletId);
                String amountStr = tx.getAmount().toString() + " " + tx.getCurrency();
                String type = isOutgoing ? "DEBIT" : "CREDIT";
                
                printer.printRecord(
                        tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "N/A",
                        tx.getId().toString(),
                        tx.getStatus().name(),
                        amountStr,
                        type
                );
            }
            
            printer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV statement", e);
        }
    }
}
