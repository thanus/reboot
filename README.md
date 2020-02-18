# ReBoot

[![Build Status](https://travis-ci.org/thanus/reboot.svg?branch=master)](https://travis-ci.org/thanus/reboot)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/cd0621ed278f46ca9be376351aeec835)](https://www.codacy.com/manual/thanus/reboot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=thanus/reboot&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/thanus/reboot/branch/master/graph/badge.svg)](https://codecov.io/gh/thanus/reboot)

A refactoring tool to automatically apply best practices in Java / Spring-Boot applications.
ReBoot performs the following refactorings on a project:

*   [Request Mapping](#request-mapping)
*   [Explicit PathVariable](#explicit-pathvariable)
*   [Field injection with Spring Autowired](#field-injection-with-spring-autowired)
*   [Field injection with Mockito](#field-injection-with-mockito)

The initial version of ReBoot was written in [Rascal](https://www.rascal-mpl.org), see branch `reboot-v1`.

## Refactorings

### Request Mapping

Before ([Full source](examples/users/src/main/java/nl/thanus/demo/controllers/UsersController.java))

```java
@RestController
@RequestMapping("/users")
public class UsersController {
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUsers() {
        // code
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        // code
    }
}
```

After

```java
@RestController
@RequestMapping("/users")
public class UsersController {
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        // code
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        // code
    }
}
```

### Explicit PathVariable

Before ([Full source](examples/users/src/main/java/nl/thanus/demo/controllers/UsersController.java))

```java
@RestController
@RequestMapping("/users")
public class UsersController {
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        // code
    }
}
```

After

```java
@RestController
@RequestMapping("/users")
public class UsersController {
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // code
    }
}
```

### Field injection with Spring Autowired

Before ([Full source](examples/users/src/main/java/nl/thanus/demo/controllers/UsersController.java))

```java
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;
    @Autowired
    private UsernameService usernameService;
}
```

After

```java
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UsersController {
    private final UsersService usersService;
    private final UsernameService usernameService;
}
```

### Field injection with Mockito

Before ([Full source](examples/users/src/test/java/nl/thanus/demo/controllers/UsersControllerTest.java))

```java
@ExtendWith(value = MockitoExtension.class)
class UsersControllerTest {
    @Mock
    private UsersService usersService;
    @Mock
    private UsernameService usernameService;
    @InjectMocks
    private UsersController usersController;
}
```

After

```java
@ExtendWith(value = MockitoExtension.class)
class UsersControllerTest {
    private UsersService usersService = Mockito.mock(UsersService.class);
    private UsernameService usernameService = Mockito.mock(UsernameService.class);
    private UsersController usersController = new UsersController();
}
```

**Note:** The constructor arguments above, `userService` and `usernameService`, are not passed to `UsersController`. 
This can easily be done manually, of course it is better if it is automated.

## Usage

### Building from source

After cloning the project, you can build it from source with:

```shell script
./mvnw clean install
```

### Running ReBoot

```shell script
java -jar target/reboot-1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/project
```
