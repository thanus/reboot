package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

internal class RequestMappingKtTest : ReBootBase() {

    @ParameterizedTest
    @MethodSource("requestMappings")
    fun `@RequestMapping with only RequestMethod should be changed to Marker Mapping Annotation`(method: String, mappingAnnotation: String) {
        val code = """
            import org.springframework.web.bind.annotation.RequestMethod;
            import org.springframework.web.bind.annotation.RequestMapping;
            
            public class UsersController {
                @RequestMapping(method = RequestMethod.$method)
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.$mappingAnnotation;
            
            public class UsersController {
                @$mappingAnnotation
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Test
    fun `@RequestMapping refactoring should not add extra GetMapping import`() {
        val code = """
            import org.springframework.web.bind.annotation.RequestMethod;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            
            public class UsersController {
                @RequestMapping(method = RequestMethod.GET)
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.GetMapping;
            
            public class UsersController {
                @GetMapping
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Test
    fun `@RequestMapping refactoring should not add a GetMapping import when it has a wildcard`() {
        val code = """
            import org.springframework.web.bind.annotation.*;
            
            public class UsersController {
                @RequestMapping(method = RequestMethod.GET)
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.*;
            
            public class UsersController {
                @GetMapping
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Test
    fun `@RequestMapping refactoring should not remove a shared RequestMapping`() {
        val code = """
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RequestMethod;
            
            @RequestMapping("/users")
            public class UsersController {
                @RequestMapping(method = RequestMethod.GET)
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            
            @RequestMapping("/users")
            public class UsersController {
                @GetMapping
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @ParameterizedTest
    @MethodSource("requestMappings")
    fun `@RequestMapping with only static RequestMethod should be changed to Marker Mapping Annotation`(method: String, mappingAnnotation: String) {
        val code = """
            import static org.springframework.web.bind.annotation.RequestMethod.$method;
            import org.springframework.web.bind.annotation.RequestMapping;
            
            public class UsersController {
                @RequestMapping(method = $method)
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.$mappingAnnotation;
            
            public class UsersController {
                @$mappingAnnotation
                public ResponseEntity<List<User>> getUsers() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @ParameterizedTest
    @ValueSource(strings = ["path", "value"])
    fun `@RequestMapping with path should be changed to Mapping Annotation with implicit path`(input: String) {
        val code = """
            import org.springframework.web.bind.annotation.RequestMethod;
            import org.springframework.web.bind.annotation.RequestMapping;
            
            public class UsersController {
                @RequestMapping($input = "/{id}", method = RequestMethod.GET)
                public ResponseEntity<User> getUser() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.GetMapping;
            
            public class UsersController {
                @GetMapping("/{id}")
                public ResponseEntity<User> getUser() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Test
    fun `@RequestMapping with multiple parameters should only change the annotation`() {
        val code = """
            import org.springframework.web.bind.annotation.RequestMethod;
            import org.springframework.web.bind.annotation.RequestMapping;
            
            public class UsersController {
                @RequestMapping(path = "/{id}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
                public ResponseEntity<User> getUser() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.GetMapping;
            
            public class UsersController {
                @GetMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
                public ResponseEntity<User> getUser() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Test
    fun `@RequestMapping should not change when method has an unknown RequestMethod expression`() {
        val code = """
            import org.springframework.web.bind.annotation.RequestMapping;
            
            public class UsersController {
                @RequestMapping(method = 5)
                public ResponseEntity<User> getUser() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteRequestMappings(compilationUnit)

        val expectedCode = """
            import org.springframework.web.bind.annotation.RequestMapping;
            
            public class UsersController {
                @RequestMapping(method = 5)
                public ResponseEntity<User> getUser() {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    private fun requestMappings(): Stream<Arguments> =
            Stream.of(
                    Arguments.of("GET", "GetMapping"),
                    Arguments.of("POST", "PostMapping"),
                    Arguments.of("PUT", "PutMapping"),
                    Arguments.of("PATCH", "PatchMapping"),
                    Arguments.of("DELETE", "DeleteMapping")
            )
}
