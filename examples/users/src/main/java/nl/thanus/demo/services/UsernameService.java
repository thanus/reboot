package nl.thanus.demo.services;

import org.springframework.stereotype.Service;

@Service
public class UsernameService {
    public String createUserName(String firstName, String lastName) {
        return firstName.substring(0, 3) + "-" + lastName.substring(0, 3);
    }
}
