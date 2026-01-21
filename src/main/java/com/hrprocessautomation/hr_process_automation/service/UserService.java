package com.hrprocessautomation.hr_process_automation.service;

import com.hrprocessautomation.hr_process_automation.model.*;
import com.hrprocessautomation.hr_process_automation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    
//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean exists(String username) {
        return userRepository.findByUsername(username) != null;
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
