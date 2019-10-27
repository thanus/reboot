package nl.thanus.demo.controllers;

import nl.thanus.demo.models.User;
import nl.thanus.demo.services.UsernameService;
import nl.thanus.demo.services.UsersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class UsersControllerTestV2 {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UsersService usersService;
    @Spy
    private UsernameService usernameService = new UsernameService();
    @InjectMocks
    private UsersController usersController;

    @Test
    void getUsersTest() {
        User user = createUser();
        List<User> users = Collections.singletonList(user);
        when(usersService.findAll()).thenReturn(users);

        ResponseEntity<List<User>> usersResponseEntity = usersController.getUsers();

        assertEquals(users, usersResponseEntity.getBody());
    }

    @Test
    void getUserTest() {
        User user = createUser();
        when(usersService.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<User> userResponseEntity = usersController.getUser(1L);

        assertEquals(user, userResponseEntity.getBody());
    }

    @Test
    void createUserTest() {
        User user = createUser();
        when(usersService.save(user)).thenReturn(user);

        ResponseEntity<User> userResponseEntity = usersController.createUser(user);

        assertEquals(user, userResponseEntity.getBody());
    }

    @Test
    void updateUserTest() {
        User user = createUser();
        when(usersService.findById(user.getId())).thenReturn(Optional.of(user));
        when(usersService.save(user)).thenReturn(user);

        ResponseEntity<User> userResponseEntity = usersController.updateUser(1L, user);

        assertEquals(user, userResponseEntity.getBody());
    }

    @Test
    void deleteUserTest() {
        User user = createUser();
        when(usersService.findById(user.getId())).thenReturn(Optional.of(user));

        ResponseEntity<Void> responseEntity = usersController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    private User createUser() {
        return new User(1L, "Foo", "Bar", "Foo-Bar");
    }
}