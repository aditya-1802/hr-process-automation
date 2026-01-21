package com.hrprocessautomation.hr_process_automation.service;

import com.hrprocessautomation.hr_process_automation.model.Offer;
import com.hrprocessautomation.hr_process_automation.repository.OfferRepository;

import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Offer not found"));
    }

    // Save or update offer
    public Offer saveOrUpdateOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    // Save + Send Offer Mail
    public void saveAndSendOffer(Offer offer) {

        // 1️⃣ Save offer first (ID required for links)
        offer.setStatus("SENT");
        offer = offerRepository.save(offer);

        // 2️⃣ Generate PDF
        byte[] pdfBytes = pdfService.generateOfferPdf(offer);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(offer.getEmail());
            helper.setSubject("Offer Letter - Tyroads Global | Ref #" + offer.getId());

            // 3️⃣ Accept / Reject links
            String acceptLink =
                    "http://localhost:8080/offer/respond/" + offer.getId() + "?action=accept";

            String rejectLink =
                    "http://localhost:8080/offer/respond/" + offer.getId() + "?action=reject";

            // 4️⃣ HTML Mail Body
            String html = """
                <div style="font-family:Segoe UI,Arial,sans-serif; line-height:1.6;">
                    <h2>Dear %s,</h2>

                    <p>
                        We are pleased to offer you the position of
                        <b>%s</b> at <b>Tyroads Global</b>.
                    </p>

                    <p>
                        <b>Offered Salary:</b> ₹%.2f
                    </p>

                    <p>Please review your offer letter attached below.</p>

                    <div style="margin:25px 0;">
                        <a href="%s"
                           style="padding:12px 22px;
                           background:#22c55e;
                           color:white;
                           text-decoration:none;
                           border-radius:6px;
                           font-weight:600;">
                            ✅ Accept Offer
                        </a>

                        &nbsp;&nbsp;

                        <a href="%s"
                           style="padding:12px 22px;
                           background:#ef4444;
                           color:white;
                           text-decoration:none;
                           border-radius:6px;
                           font-weight:600;">
                            ❌ Reject Offer
                        </a>
                    </div>

                    <p style="margin-top:30px;">
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

            // 5️⃣ Set HTML (IMPORTANT)
            helper.setText(html, true);

            // 6️⃣ Attach PDF
            helper.addAttachment(
                    "Offer_Letter_" + offer.getName() + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            // 7️⃣ Send mail
            mailSender.send(message);

        } catch (Exception e) {
            offer.setStatus("FAILED");
            offerRepository.save(offer);
            throw new RuntimeException("Failed to send offer mail", e);
        }
    }
    
    public List<Offer> getAllOffers() {
        return offerRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Offer> getOffersByStatus(String status) {
        return offerRepository.findByStatus(status);
    }

    public List<Offer> getOffersByResponse(String status) {
		return offerRepository.findByCandidateResponse(status);
	}


}
