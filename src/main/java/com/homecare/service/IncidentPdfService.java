package com.homecare.service;

import com.homecare.entity.Incident;
import com.homecare.entity.IncidentAttachment;
import com.homecare.entity.IncidentReview;
import com.homecare.repository.IncidentAttachmentRepository;
import com.homecare.repository.IncidentRepository;
import com.homecare.repository.IncidentReviewRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IncidentPdfService {

    private final IncidentRepository incidentRepository;
    private final IncidentReviewRepository incidentReviewRepository;
    private final IncidentAttachmentRepository incidentAttachmentRepository;

    public IncidentPdfService(
            IncidentRepository incidentRepository,
            IncidentReviewRepository incidentReviewRepository,
            IncidentAttachmentRepository incidentAttachmentRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentReviewRepository = incidentReviewRepository;
        this.incidentAttachmentRepository = incidentAttachmentRepository;
    }

    public byte[] generateIncidentPdf(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found."));

        IncidentReview review = incidentReviewRepository.findByIncidentId(incidentId)
                .orElse(null);

        List<IncidentAttachment> attachments =
                incidentAttachmentRepository.findByIncidentIdOrderByUploadedAtDesc(incidentId);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.LETTER, 40, 40, 40, 40);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Incident Report", titleFont));
            document.add(new Paragraph(" "));

            addLine(document, "Client", incident.getClient().getFullName(), normalFont);
            addLine(document, "Caregiver", incident.getCaregiver().getFullName(), normalFont);
            addLine(document, "Incident Type", incident.getIncidentType(), normalFont);
            addLine(document, "Severity", incident.getSeverity(), normalFont);
            addLine(document, "Status", incident.getStatus(), normalFont);
            addLine(document, "State Reportable", Boolean.TRUE.equals(incident.getStateReportable()) ? "Yes" : "No", normalFont);

            if (incident.getIncidentDateTime() != null) {
                addLine(document, "Incident Date/Time", formatDate(incident.getIncidentDateTime()), normalFont);
            }

            document.add(new Paragraph(" "));

            addSection(document, "Incident Description", incident.getDescription(), sectionFont, normalFont);
            addSection(document, "Immediate Action Taken", incident.getImmediateActionTaken(), sectionFont, normalFont);
            addSection(document, "Witness Name", incident.getWitnessName(), sectionFont, normalFont);
            addSection(document, "Witness Phone", incident.getWitnessPhone(), sectionFont, normalFont);
            addSection(document, "Witness Statement", incident.getWitnessStatement(), sectionFont, normalFont);

            if (review != null) {
                addSection(document, "Supervisor Notes", review.getSupervisorNotes(), sectionFont, normalFont);
                addSection(document, "Corrective Action", review.getCorrectiveAction(), sectionFont, normalFont);
                addSection(document, "Follow-Up Required", review.getFollowUpRequired(), sectionFont, normalFont);

                if (review.getReviewedAt() != null) {
                    addLine(document, "Reviewed At", formatDate(review.getReviewedAt()), normalFont);
                }
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Attachments", sectionFont));

            if (attachments.isEmpty()) {
                document.add(new Paragraph("No attachments uploaded.", normalFont));
            } else {
                for (IncidentAttachment attachment : attachments) {
                    document.add(new Paragraph(
                            attachment.getFileName() + " (" + attachment.getFileType() + ")",
                            normalFont
                    ));

                    if (attachment.getFileType() != null &&
                            attachment.getFileType().startsWith("image")) {
                        try {
                            Image image = Image.getInstance(attachment.getFilePath());
                            image.scaleToFit(400, 250);
                            document.add(image);
                        } catch (Exception imageError) {
                            document.add(new Paragraph("Image could not be embedded.", normalFont));
                        }
                    } else {
                        document.add(new Paragraph("Non-image attachment listed only.", normalFont));
                    }

                    document.add(new Paragraph(" "));
                }
            }

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate incident PDF.");
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