package com.hrprocessautomation.hr_process_automation.repository;

import com.hrprocessautomation.hr_process_automation.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Find offer by candidate email
    Optional<Offer> findByEmail(String email);

    // Find all offers by position
    List<Offer> findByPosition(String position);

    // Find offers by status (SENT, ACCEPTED, REJECTED)
    List<Offer> findByStatus(String status);

    // Check if offer already exists for an email
    boolean existsByEmail(String email);

	List<Offer> findByCandidateResponse(String status);
}
