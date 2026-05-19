package com.homecare.controller;

import com.homecare.dto.ClientPayrollResponse;
import com.homecare.service.ClientPayrollService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin("*")
public class InvoicePdfController {

    private final ClientPayrollService clientPayrollService;

    public InvoicePdfController(ClientPayrollService clientPayrollService) {
        this.clientPayrollService = clientPayrollService;
    }

    @GetMapping("/client/{clientId}/pdf")
    public ResponseEntity<byte[]> generateClientInvoicePdf(
            @PathVariable Long clientId,
            @RequestParam Double rate
    ) {
        try {
            ClientPayrollResponse payroll =
                    clientPayrollService.calculateClientPayroll(clientId, rate);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("HomeCare AI - Client Invoice", titleFont);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Client Name: " + payroll.getClientName(), normalFont));
            document.add(new Paragraph("Client ID: " + payroll.getClientId(), normalFont));
            document.add(new Paragraph("Hourly Rate: $" + String.format("%.2f", payroll.getHourlyRate()), normalFont));
            document.add(new Paragraph("Total Hours: " + String.format("%.2f", payroll.getTotalHours()), normalFont));
            document.add(new Paragraph("Amount Due: $" + String.format("%.2f", payroll.getAmountDue()), headingFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice Summary", headingFont));
            document.add(new Paragraph(
                    "This invoice was generated based on completed EVV clock records linked to the client.",
                    normalFont
            ));

            document.close();

            byte[] pdfBytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=client-invoice-" + clientId + ".pdf"
                    )
                    .body(pdfBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage());
        }
    }
}