package com.hrprocessautomation.hr_process_automation.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public void sendOfferEmail(
            String toEmail,
            String name,
            String subject,
            String htmlContent,
            byte[] pdfBytes
    ) throws Exception {

        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        String json = """
        {
          "sender": {
            "name": "HR Team",
            "email": "hr@tyroadsglobal.com"
          },
          "to": [{
            "email": "%s",
            "name": "%s"
          }],
          "subject": "%s",
          "htmlContent": %s,
          "attachment": [{
            "content": "%s",
            "name": "Offer_Letter.pdf"
          }]
        }
        """.formatted(
                toEmail,
                name,
                subject,
                jsonEscape(htmlContent),
                pdfBase64
        );

        Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .addHeader("api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Brevo failed: " + response.body().string()
                );
            }
        }
    }

    private String jsonEscape(String html) {
        return "\"" + html
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "") + "\"";
    }
}
