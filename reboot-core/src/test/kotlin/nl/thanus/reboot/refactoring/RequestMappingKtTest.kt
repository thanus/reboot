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
            public class UsersController {
                $mappingAnnotation
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
            public class UsersController {
                $mappingAnnotation
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
                    Arguments.of("GET", "@GetMapping"),
                    Arguments.of("POST", "@PostMapping"),
                    Arguments.of("PUT", "@PutMapping"),
                    Arguments.of("PATCH", "@PatchMapping"),
                    Arguments.of("DELETE", "@DeleteMapping")
            )
}
