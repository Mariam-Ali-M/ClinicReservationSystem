package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import model.Appointment;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    public static void exportAppointmentsToPDF(List<Appointment> appointments, String fileName) throws Exception {
        Document document = new Document(PageSize.A4.rotate()); // Landscape
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // العنوان
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Appointments Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph date = new Paragraph("Generated on: " + java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' hh:mm a")),
                FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);
        document.add(new Paragraph(" "));

        // الجدول: 7 أعمدة
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        float[] columnWidths = {1.5f, 1.5f, 1.2f, 2f, 1.5f, 1.5f, 1.5f};
        table.setWidths(columnWidths);

        // رؤوس الأعمدة
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        String[] headers = {"Date", "Time", "Type", "Patient", "Status", "Valid Until", "Price"};

        for (String headerText : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
            cell.setBackgroundColor(new BaseColor(21, 191, 143)); // #15BF8F
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // البيانات
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (Appointment appt : appointments) {
            // التاريخ
            String dateStr = (appt.getAppointmentDateTime() != null && appt.getAppointmentDateTime().getDate() != null)
                    ? appt.getAppointmentDateTime().getDate().format(DateTimeFormatter.ofPattern("dd/MM"))
                    : "—";
            table.addCell(new Phrase(dateStr, dataFont));

            // الوقت
            String timeStr = (appt.getAppointmentDateTime() != null)
                    ? appt.getAppointmentDateTime().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "–"
                    + appt.getAppointmentDateTime().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "—";
            table.addCell(new Phrase(timeStr, dataFont));

            // النوع
            String type = appt.getAppointmentType() != null
                    ? appt.getAppointmentType().toString()
                    : "Visit";
            table.addCell(new Phrase(type, dataFont));

            // المريض
            String patient = (appt.getPatient() != null && appt.getPatient().getName() != null)
                    ? appt.getPatient().getName()
                    : "Unknown";
            table.addCell(new Phrase(patient, dataFont));

            // الحالة
            String status = (appt.getStatus() != null) ? appt.getStatus().toString() : "—";
            table.addCell(new Phrase(status, dataFont));

            // Valid Until
            String validUntil = (appt.getConsultationExpiryDate() != null)
                    ? appt.getConsultationExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM"))
                    : "—";
            table.addCell(new Phrase(validUntil, dataFont));

            // السعر
            String price = (appt.getClinic() != null)
                    ? String.format("%.2f EGP", appt.getClinic().getPrice())
                    : "—";
            table.addCell(new Phrase(price, dataFont));
        }

        document.add(table);

        // التذييل
        Paragraph footer = new Paragraph(
                "Total: " + appointments.size() + " appointment(s)\nDoCC Medical System",
                FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(new Paragraph(" "));
        document.add(footer);

        document.close();
    }
}