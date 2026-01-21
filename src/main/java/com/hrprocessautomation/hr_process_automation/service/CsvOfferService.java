package com.hrprocessautomation.hr_process_automation.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hrprocessautomation.hr_process_automation.model.Offer;

@Service
public class CsvOfferService {

    public List<Offer> readOffers(MultipartFile file) throws Exception {

        List<Offer> offers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] data = line.split(",");

                Offer offer = new Offer();
                offer.setName(data[0]);
                offer.setEmail(data[1]);
                offer.setPosition(data[2]);
                offer.setSalary(Double.parseDouble(data[3]));
                offer.setStatus("CREATED");

                offers.add(offer);
            }
        }
        return offers;
    }
}

