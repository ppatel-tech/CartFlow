package com.cartflow.order.service;

import com.cartflow.exception.BusinessException;
import com.cartflow.order.entity.Order;
import com.cartflow.order.entity.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {

    public byte[] generateInvoicePdf(Order order, List<OrderItem> items) {

        Document document = new Document();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font headingFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            document.add(new Paragraph("CartFlow - Invoice", titleFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Order Number: " + order.getOrderNumber(), headingFont));
            document.add(new Paragraph("Order Date: " + formatDate(order), normalFont));
            document.add(new Paragraph("Customer: " + order.getCustomer().getFirstName()
                    + " " + order.getCustomer().getLastName(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Shipping Address:", headingFont));
            document.add(new Paragraph(order.getShippingFullName(), normalFont));
            document.add(new Paragraph(order.getShippingStreet(), normalFont));
            document.add(new Paragraph(order.getShippingCity() + ", " + order.getShippingState()
                    + " " + order.getShippingPostalCode(), normalFont));
            document.add(new Paragraph(order.getShippingCountry(), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Product");
            table.addCell("Quantity");
            table.addCell("Price");
            table.addCell("Subtotal");

            for (OrderItem item : items) {
                table.addCell(item.getProductName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getSellingPrice().toString());
                table.addCell(item.getSellingPrice()
                        .multiply(java.math.BigDecimal.valueOf(item.getQuantity()))
                        .toString());
            }

            document.add(table);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Subtotal: " + order.getSubtotal(), normalFont));
            document.add(new Paragraph("Discount: -" + order.getDiscount(), normalFont));
            document.add(new Paragraph("Tax: " + order.getTax(), normalFont));
            document.add(new Paragraph("Shipping: " + order.getShippingCharge(), normalFont));
            document.add(new Paragraph("Total: " + order.getFinalAmount(), headingFont));

            document.close();

            return out.toByteArray();

        } catch (Exception ex) {
            throw new BusinessException("Failed to generate invoice: " + ex.getMessage());
        }
    }

    private String formatDate(Order order) {
        return order.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}