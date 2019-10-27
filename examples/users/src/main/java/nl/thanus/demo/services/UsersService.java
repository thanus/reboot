package nl.thanus.demo.services;

import nl.thanus.demo.models.User;
import nl.thanus.demo.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    public List<User> findAll() {
        return usersRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return usersRepository.findById(id);
    }

    public User save(User user) {
        return usersRepository.save(user);
    }

    public void delete(User user) {
        usersRepository.delete(user);
    }
}
