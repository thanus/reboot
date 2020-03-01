# ReBoot

[![Build Status](https://travis-ci.org/thanus/reboot.svg?branch=master)](https://travis-ci.org/thanus/reboot)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/cd0621ed278f46ca9be376351aeec835)](https://www.codacy.com/manual/thanus/reboot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=thanus/reboot&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/thanus/reboot/branch/master/graph/badge.svg)](https://codecov.io/gh/thanus/reboot)

A refactoring tool to automatically apply best practices in Java / Spring-Boot applications.
ReBoot performs the following refactorings on a project:

*   [Request Mapping](#request-mapping)
*   [Explicit web annotation value (PathVariable, RequestParam, RequestHeader, etc)](#explicit-web-annotation)
*   [Explicit mandatory web annotation (PathVariable, RequestParam, RequestHeader, etc)](#explicit-mandatory-web-annotation)
*   [Field injection with Spring Autowired](#field-injection-with-spring-autowired)
*   [Field injection with Mockito](#field-injection-with-mockito)

The initial version of ReBoot was written in [Rascal](https://www.rascal-mpl.org), see branch `reboot-v1`.

## Refactorings

### Request Mapping

Spring provides HTTP method specific [shortcut variants](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-requestmapping)
for `@RequestMapping`. These custom annotations (`@GetMapping`, `@PostMapping`, etc) are less verbose and more
expressive than `@RequestMapping`. This refactoring is also applied to projects using [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign),
as they also reuse Spring annotations.

#### Refactoring diff

```diff
-import org.springframework.web.bind.annotation.RequestMethod;
+import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/users")
public class UsersController {
-   @RequestMapping(method = RequestMethod.GET)
+   @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        // code
    }

-   @RequestMapping(path = "/{id}", method = RequestMethod.GET)
+   @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        // code
    }
}
```

### Explicit web annotation

URI Variables can be named [explicitly](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-requestmapping-uri-templates),
like `@PathVariable("id") Long id`, but this is redundant. This detail can be left out if the names are the same. This
refactoring is also applied to projects using [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign),
as they also reuse Spring annotations. This refactoring is not only applicable for [PathVariable](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/PathVariable.html)
but also [RequestParam](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestParam.html), 
[RequestHeader](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestHeader.html), 
[RequestAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestAttribute.html), 
[CookieValue](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/CookieValue.html), 
[ModelAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ModelAttribute.html), 
[SessionAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/SessionAttribute.html).
For example, for `@PathVariable`:

#### Refactoring diff

```diff
@RestController
@RequestMapping("/users")
public class UsersController {
    @GetMapping("/{id}")
-   public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
+   public ResponseEntity<User> getUser(@PathVariable Long id) {
        // code
    }
}
```

### Explicit mandatory web annotation

The attribute `required` set to true on annotations like `@PathVariable` is not necessary as this is default already. 
This refactoring is also applied to projects using [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign),
as they also reuse Spring annotations. This refactoring is not only applicable for [PathVariable](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/PathVariable.html)
but also [RequestParam](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestParam.html), 
[RequestHeader](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestHeader.html), 
[RequestAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestAttribute.html), 
[CookieValue](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/CookieValue.html), 
[ModelAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ModelAttribute.html), 
[SessionAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/SessionAttribute.html).
For example, for `@PathVariable`:

#### Refactoring diff

```diff
@RestController
@RequestMapping("/users")
public class UsersController {
    @GetMapping("/{id}")
-   public ResponseEntity<User> getUser(@PathVariable(required = true) Long id) {
+   public ResponseEntity<User> getUser(@PathVariable Long id) {
        // code
    }
}
```

### Field injection with Spring Autowired

Dependency injection with field injection is not recommended. Instead, constructor injection should be used, leading
to safer code and easier to test. This is explained in more detail in article [why-field-injection-is-evil](http://olivergierke.de/2013/11/why-field-injection-is-evil/).

#### Refactoring diff

```diff
-import org.springframework.beans.factory.annotation.Autowired;
+import lombok.RequiredArgsConstructor;

+@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UsersController {
-   @Autowired
-   private UsersService usersService;
-   @Autowired
-   private UsernameService usernameService;
+   private final UsersService usersService;
+   private final UsernameService usernameService;
}
```

### Field injection with Mockito

Just like the above refactoring, it is not recommended to do field injection with Mockito for the same reasons.
This is explained in more detail in article [Mockito: Why You Should Not Use InjectMocks Annotation to Autowire Fields](https://tedvinke.wordpress.com/2014/02/13/mockito-why-you-should-not-use-injectmocks-annotation-to-autowire-fields/).

#### Refactoring diff

```diff
-import org.mockito.InjectMocks;
-import org.mockito.Mock;
+import org.mockito.Mockito;

@ExtendWith(value = MockitoExtension.class)
class UsersControllerTest {
-   @Mock
-   private UsersService usersService;
-   @Mock
-   private UsernameService usernameService;
-   @InjectMocks
-   private UsersController usersController;
+   private UsersService usersService = Mockito.mock(UsersService.class);
+   private UsernameService usernameService = Mockito.mock(UsernameService.class);
+   private UsersController usersController = new UsersController();
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

### Running ReBoot jar

```shell script
cd reboot-core
java -jar target/reboot-core-0.1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/project
```

### Running reboot-maven-plugin

Add reboot-maven-plugin to your POM:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>nl.thanus</groupId>
            <artifactId>reboot-maven-plugin</artifactId>
            <version>0.1.0</version>
        </plugin>
    </plugins>
</build>
```

By default, reboot-maven-plugin uses `${project.basedir}` as path to refactor. If you want to specify another path, you
can use the property `directory`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>nl.thanus</groupId>
            <artifactId>reboot-maven-plugin</artifactId>
            <version>0.1.0</version>
            <configuration>
                <directory>/path/to/project</directory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Run plugin:

```shell script
mvn nl.thanus:reboot-maven-plugin:0.1.0:reboot
```

## Contributions are welcome!
Feel free to suggest and implement improvements.
