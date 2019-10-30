package nl.thanus.demo.controllers;

import nl.thanus.demo.models.User;
import nl.thanus.demo.services.UsernameService;
import nl.thanus.demo.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;
    @Autowired
    private UsernameService usernameService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(usersService.findAll());
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        final Optional<User> optionalUser = usersService.findById(id);
        return optionalUser.map(ResponseEntity::ok)
                .orElseThrow(UserNotFoundException::new);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        user.setUsername(usernameService.createUserName(user.getFirstName(), user.getLastName()));
        return ResponseEntity.ok(usersService.save(user));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        final Optional<User> optionalUser = usersService.findById(id);

        return optionalUser.map(existingUser -> {
            final String username = usernameService.createUserName(user.getFirstName(), user.getLastName());
            final User updatedUser = new User(existingUser.getId(), user.getFirstName(), user.getLastName(), username);

            return ResponseEntity.ok(usersService.save(updatedUser));
        }).orElseThrow(UserNotFoundException::new);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        final Optional<User> optionalUser = usersService.findById(id);

        return optionalUser.map(user -> {
            usersService.delete(user);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }).orElseThrow(UserNotFoundException::new);
    }
}
