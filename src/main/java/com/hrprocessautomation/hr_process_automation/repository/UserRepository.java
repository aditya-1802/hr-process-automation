package com.hrprocessautomation.hr_process_automation.repository;

import com.hrprocessautomation.hr_process_automation.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
