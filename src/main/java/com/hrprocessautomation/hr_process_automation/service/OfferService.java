package com.hrprocessautomation.hr_process_automation.service;

import com.hrprocessautomation.hr_process_automation.model.Offer;
import com.hrprocessautomation.hr_process_automation.repository.OfferRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OfferPdfService pdfService;

    // Get offer by ID
    public Offer getOfferById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
    }

    // Save or update offer
    public Offer saveOrUpdateOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    // Save offer + Send mail
    public void saveAndSendOffer(Offer offer) {

        // 1️⃣ Save offer first
        offer.setStatus("SENT");
        offer = offerRepository.save(offer);

        // 2️⃣ Generate PDF
        byte[] pdfBytes = pdfService.generateOfferPdf(offer);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(offer.getEmail());
            helper.setSubject("Offer Letter | Ref #" + offer.getId());
            helper.setFrom("hr@yourdomain.com");

            // 3️⃣ Accept / Reject links
            String baseUrl = "http://localhost:8080"; // change after deploy
            String acceptLink = baseUrl + "/offer/respond/" + offer.getId() + "?action=accept";
            String rejectLink = baseUrl + "/offer/respond/" + offer.getId() + "?action=reject";

            // 4️⃣ HTML Body
            String html = """
                <div style="font-family:Arial,sans-serif; line-height:1.6;">
                    <h2>Dear %s,</h2>

                    <p>
                        We are pleased to offer you the position of
                        <b>%s</b> at <b>Tyroads Global</b>.
                    </p>

                    <p><b>Offered Salary:</b> ₹%.2f</p>

                    <p>Please find your offer letter attached.</p>

                    <div style="margin:25px 0;">
                        <a href="%s"
                           style="padding:12px 22px;background:#22c55e;color:#fff;
                           text-decoration:none;border-radius:6px;">
                            ✅ Accept Offer
                        </a>

                        &nbsp;&nbsp;

                        <a href="%s"
                           style="padding:12px 22px;background:#ef4444;color:#fff;
                           text-decoration:none;border-radius:6px;">
                            ❌ Reject Offer
                        </a>
                    </div>

                    <p>
                        Regards,<br>
                        <b>HR Team</b><br>
                        Tyroads Global
                    </p>
                </div>
            """.formatted(
                    offer.getName(),
                    offer.getPosition(),
                    offer.getSalary(),
                    acceptLink,
                    rejectLink
            );

            helper.setText(html, true);

            // 5️⃣ Attach PDF
            helper.addAttachment(
                    "Offer_Letter_" + offer.getName() + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            // 6️⃣ Send mail
            mailSender.send(message);

        } catch (Exception e) {
            offer.setStatus("FAILED");
            offerRepository.save(offer);
            throw new RuntimeException("Failed to send offer mail", e);
        }
    }

    public List<Offer> getAllOffers() {
        return offerRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Offer> getOffersByStatus(String status) {
        return offerRepository.findByStatus(status);
    }

    public List<Offer> getOffersByResponse(String response) {
        return offerRepository.findByCandidateResponse(response);
    }
}
