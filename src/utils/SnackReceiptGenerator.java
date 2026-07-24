package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import models.SnackSale;
import models.SnackSaleItem;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.math.BigDecimal;

public class SnackReceiptGenerator {

    public static void generateAndOpenReceipt(SnackSale sale, List<SnackSaleItem> items) {
        try {
            // Ensure directory exists
            File dir = new File("receipts");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            StringBuilder receiptText = new StringBuilder();
            receiptText.append("============= CineSphere =============\n");
            receiptText.append("            SNACK RECEIPT             \n");
            receiptText.append("======================================\n");
            receiptText.append("Sale ID: ").append(sale.getId()).append("\n");
            receiptText.append("Receipt No: RCPT-").append(sale.getId()).append("\n");
            receiptText.append("Date & Time: ").append(sale.getSaleTime() != null ? sale.getSaleTime().toString() : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            
            String cashier = sale.getCashierName();
            if (cashier == null || cashier.isEmpty()) { cashier = "Admin"; }
            receiptText.append("Cashier: ").append(cashier).append("\n");
            
            if (sale.getBookingId() != null && sale.getBookingId() > 0) {
                receiptText.append("Booking ID: BK-").append(sale.getBookingId()).append("\n");
            }
            
            receiptText.append("--------------------------------------\n");
            receiptText.append("Purchased Items:\n");
            
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal discountTotal = BigDecimal.ZERO;

            for (SnackSaleItem item : items) {
                receiptText.append(item.getSnackName()).append("\n");
                receiptText.append("  ").append(item.getQuantity()).append(" x $").append(item.getPriceAtSale());
                receiptText.append("  = $").append(item.getLineTotal()).append("\n");
                
                subtotal = subtotal.add(item.getPriceAtSale().multiply(new BigDecimal(item.getQuantity())));
            }
            
            discountTotal = subtotal.subtract(sale.getTotalAmount());
            
            receiptText.append("--------------------------------------\n");
            if (discountTotal.compareTo(BigDecimal.ZERO) > 0) {
                receiptText.append("Subtotal: $").append(subtotal).append("\n");
                receiptText.append("Discount: -$").append(discountTotal).append("\n");
            }
            receiptText.append("Grand Total: $").append(sale.getTotalAmount()).append("\n");
            receiptText.append("======================================\n");

            // Generate QR Code image
            String fileName = "receipts/Receipt_" + sale.getId() + ".png";
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(receiptText.toString(), BarcodeFormat.QR_CODE, 400, 400);
            
            Path path = FileSystems.getDefault().getPath(fileName);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            // Open the generated QR code image
            File fileToOpen = new File(fileName);
            if (fileToOpen.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(fileToOpen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
