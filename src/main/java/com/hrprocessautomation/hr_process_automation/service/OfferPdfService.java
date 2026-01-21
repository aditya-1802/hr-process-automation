package com.hrprocessautomation.hr_process_automation.service;

import com.hrprocessautomation.hr_process_automation.model.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class OfferPdfService {

    public byte[] generateOfferPdf(Offer offer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        document.add(new Paragraph("OFFER LETTER")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        // Body
        document.add(new Paragraph("Dear " + offer.getName() + ",\n\n"));

        document.add(new Paragraph(
                "We are pleased to offer you the position of " + offer.getPosition() +
                " with an annual salary of ₹" + offer.getSalary() + ".\n\n"
        ));

        document.add(new Paragraph(
                "Your employment with us will be subject to company policies and procedures.\n\n"
        ));

        document.add(new Paragraph(
                "We look forward to having you as part of our organization.\n\n"
        ));

        document.add(new Paragraph("Best Regards,"));
        document.add(new Paragraph("HR Team"));
        document.add(new Paragraph("Tyroads Global"));

        document.close();

        return baos.toByteArray();
    }
}

