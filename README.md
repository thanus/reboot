# ReBoot

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f1d8367707be4b9193dc8b3dadd0bc6b)](https://app.codacy.com/manual/tthanusijan/reboot?utm_source=github.com&utm_medium=referral&utm_content=thanus/reboot&utm_campaign=Badge_Grade_Dashboard)

A refactoring tool to automatically apply best practices in Java / Spring-Boot applications.
ReBoot performs the following refactorings on a project:

*   [HTTP Mapping](#HTTP-Mapping)
*   [Field injection with Spring Autowired](#Field-injection-with-Spring-Autowired)
*   [Field injection with Mockito](#Field-injection-with-Mockito)

## Refactorings

### HTTP Mapping

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

### Requirements

*   [Rascal](https://www.rascal-mpl.org). Follow the installation steps on the page [Rascal MPL Start](https://www.rascal-mpl.org/start/).
*   JDK >= 1.8

### Import project in Eclipse

1.  Start Eclipse
2.  Click on `File`
    *   Click on `Open Projects from File System...`
    *   Import the project through the `Directory...`
    *   Complete the steps through the wizard

### Run

1.  Open the file [ReBoot.rsc](src/ReBoot.rsc) from the `Rascal Navigator`
2.  Right click in the editor and select the `Start Console` option. This will start a console (Rascal REPL).
3.  Import ReBoot module in console `import ReBoot;`
4.  Run the module by calling the [main](src/ReBoot.rsc#L14) function with the path to the project to be refactored.
    For example, for the [example project](examples/users) in this repo:
        main("/path/to/project/reboot/examples/users");
    Replace `/path/to/project` of the path with the right path.
5.  Hitting `Enter` will run the main function. When the refactoring is finished, it prints to the console that it is
    completed.

### CLI

1.  Clone the ReBoot project
2.  Download the standalone commandline console from the Rascal MPL website: [rascal-shell-unstable.jar](https://update.rascal-mpl.org/console/rascal-shell-unstable.jar)
    *   Currently, the project makes use of the unstable branch of Rascal
3.  Put the `rascal-shell-unstable.jar` file into the root of the ReBoot project
    *   Where the *ReBoot.rsc* file resides
4.  Run the project like follow: *`java -Xmx1G -Xss32m -jar rascal-shell-unstable.jar ReBoot.rsc <path>`*
    *   Replace the `<path>` with the path to your project
