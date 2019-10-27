package nl.thanus.demo.repositories;

import nl.thanus.demo.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UsersRepository extends CrudRepository<User, Long> {
    List<User> findAll();
}
