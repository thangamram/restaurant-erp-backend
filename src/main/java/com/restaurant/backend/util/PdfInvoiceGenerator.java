package com.restaurant.backend.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.restaurant.backend.entity.Bill;
import com.restaurant.backend.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class PdfInvoiceGenerator {

    public byte[] generateInvoicePdf(Bill bill) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.DARK_GRAY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            // Restaurant Header
            Paragraph header = new Paragraph("ROYAL GOURMET RESTAURANT & LOUNGE", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph address = new Paragraph("123 Culinary Boulevard, Tech Park, City | GSTIN: GSTIN33AAACR1234F1Z5\nPhone: +1 (555) 019-2834 | Email: info@royalgourmet.com", subtitleFont);
            address.setAlignment(Element.ALIGN_CENTER);
            address.setSpacingAfter(15);
            document.add(address);

            // Line Divider
            Paragraph divider = new Paragraph("----------------------------------------------------------------------------------------------------------------------------------");
            divider.setSpacingAfter(10);
            document.add(divider);

            // Bill & Order Metadata Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1f, 1f});

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBorder(Rectangle.NO_BORDER);
            cellLeft.addElement(new Paragraph("Invoice No: " + bill.getInvoiceNumber(), boldFont));
            cellLeft.addElement(new Paragraph("Order No: " + bill.getOrder().getOrderNumber(), normalFont));
            cellLeft.addElement(new Paragraph("Date: " + bill.getGeneratedAt().format(formatter), normalFont));
            metaTable.addCell(cellLeft);

            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(Rectangle.NO_BORDER);
            cellRight.addElement(new Paragraph("Customer: " + bill.getCustomer().getFullName() + " (" + bill.getCustomer().getMobileNumber() + ")", normalFont));
            cellRight.addElement(new Paragraph("Table: " + (bill.getOrder().getTable() != null ? bill.getOrder().getTable().getTableNumber() : "Takeaway"), normalFont));
            cellRight.addElement(new Paragraph("Cashier: " + (bill.getCashier() != null ? bill.getCashier().getFullName() : "System"), normalFont));
            metaTable.addCell(cellRight);

            document.add(metaTable);
            document.add(new Paragraph("\n"));

            // Items Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 1.5f, 1.5f});

            // Table Headers
            String[] headers = {"Item Description", "Qty", "Unit Price ($)", "Subtotal ($)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
                cell.setBackgroundColor(new Color(230, 230, 230));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Rows
            for (OrderItem item : bill.getOrder().getItems()) {
                PdfPCell nameCell = new PdfPCell(new Phrase(item.getItemName(), normalFont));
                nameCell.setPadding(5);
                table.addCell(nameCell);

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                qtyCell.setPadding(5);
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(qtyCell);

                PdfPCell priceCell = new PdfPCell(new Phrase(String.format("%.2f", item.getUnitPrice()), normalFont));
                priceCell.setPadding(5);
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(priceCell);

                PdfPCell subCell = new PdfPCell(new Phrase(String.format("%.2f", item.getSubtotal()), normalFont));
                subCell.setPadding(5);
                subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(subCell);
            }

            document.add(table);

            // Calculation Summary
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setWidths(new float[]{2f, 1.5f});
            summaryTable.setSpacingBefore(10);

            addSummaryRow(summaryTable, "Item Total:", String.format("$%.2f", bill.getItemTotal()), normalFont);
            addSummaryRow(summaryTable, "GST Tax:", String.format("$%.2f", bill.getGstAmount()), normalFont);
            addSummaryRow(summaryTable, "Service Charge:", String.format("$%.2f", bill.getServiceCharge()), normalFont);
            if (bill.getDiscountAmount().doubleValue() > 0) {
                addSummaryRow(summaryTable, "Discount (" + (bill.getCouponCode() != null ? bill.getCouponCode() : "Special") + "):", String.format("-$%.2f", bill.getDiscountAmount()), normalFont);
            }
            addSummaryRow(summaryTable, "Round Off:", String.format("$%.2f", bill.getRoundOff()), normalFont);
            addSummaryRow(summaryTable, "Grand Total:", String.format("$%.2f", bill.getGrandTotal()), boldFont);

            document.add(summaryTable);

            // Footer
            Paragraph footer = new Paragraph("\nThank you for dining with Royal Gourmet!\nWe look forward to serving you again soon.", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}
