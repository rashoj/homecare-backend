package com.homecare.service;

import com.homecare.entity.ServiceDocumentation;
import com.homecare.repository.ServiceDocumentationRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class ServiceDocumentationPdfService {

    private final ServiceDocumentationRepository serviceDocumentationRepository;

    public ServiceDocumentationPdfService(
            ServiceDocumentationRepository serviceDocumentationRepository
    ) {
        this.serviceDocumentationRepository = serviceDocumentationRepository;
    }

    public byte[] generatePdf(Long id) {
        ServiceDocumentation documentation =
                serviceDocumentationRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Service documentation not found."));

        if (!Boolean.TRUE.equals(documentation.getLocked())
                || !"APPROVED".equalsIgnoreCase(documentation.getStatus())) {
            throw new RuntimeException("Only approved and locked documentation can be exported.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.LETTER, 40, 40, 40, 40);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Service Documentation Record", titleFont));
            document.add(new Paragraph(" "));

            addLine(document, "Client", documentation.getClient().getFullName(), normalFont);
            addLine(document, "Caregiver", documentation.getCaregiver().getFullName(), normalFont);
            addLine(document, "Status", documentation.getStatus(), normalFont);
            addLine(document, "Locked", documentation.getLocked() ? "Yes" : "No", normalFont);

            if (documentation.getSubmittedAt() != null) {
                addLine(document, "Submitted At", formatDate(documentation.getSubmittedAt()), normalFont);
            }

            if (documentation.getApprovedAt() != null) {
                addLine(document, "Approved At", formatDate(documentation.getApprovedAt()), normalFont);
            }

            document.add(new Paragraph(" "));

            addSection(document, "Shift Tasks Completed", documentation.getShiftTasksCompleted(), sectionFont, normalFont);
            addSection(document, "ADLs Completed", documentation.getAdlsCompleted(), sectionFont, normalFont);
            addSection(document, "Goal Progress Notes", documentation.getGoalProgressNotes(), sectionFont, normalFont);
            addSection(document, "Daily Service Notes", documentation.getDailyServiceNotes(), sectionFont, normalFont);
            addSection(document, "Supervisor Comments", documentation.getSupervisorComments(), sectionFont, normalFont);

            document.add(new Paragraph("Caregiver Signature", sectionFont));

            if (documentation.getCaregiverSignature() != null
                    && documentation.getCaregiverSignature().startsWith("data:image")) {

                String base64 = documentation.getCaregiverSignature()
                        .substring(documentation.getCaregiverSignature().indexOf(",") + 1);

                byte[] imageBytes = Base64.getDecoder().decode(base64);

                Image signatureImage = Image.getInstance(imageBytes);
                signatureImage.scaleToFit(250, 100);

                document.add(signatureImage);
            } else {
                document.add(new Paragraph(
                        documentation.getCaregiverSignature() != null
                                ? documentation.getCaregiverSignature()
                                : "Not provided",
                        normalFont
                ));
            }

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate service documentation PDF.");
        }
    }

    private void addLine(Document document, String label, String value, Font font)
            throws DocumentException {
        document.add(new Paragraph(label + ": " + (value != null ? value : "—"), font));
    }

    private void addSection(
            Document document,
            String title,
            String value,
            Font sectionFont,
            Font normalFont
    ) throws DocumentException {
        document.add(new Paragraph(title, sectionFont));
        document.add(new Paragraph(value != null ? value : "Not provided", normalFont));
        document.add(new Paragraph(" "));
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
    }
}