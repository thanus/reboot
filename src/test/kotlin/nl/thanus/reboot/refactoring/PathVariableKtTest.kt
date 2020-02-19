package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PathVariableKtTest : ReBootBase() {

    @Test
    fun `PathVariable value with same variable name should be implicit`() {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewritePathVariable(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `PathVariable value should be explicit when it is not the same as variable name`() {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable("userId") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewritePathVariable(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable("userId") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @ParameterizedTest
    @ValueSource(strings = ["name", "value"])
    fun `PathVariable with explicit value which is the same as variable name should be implicit`(input: String) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable($input = "id") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewritePathVariable(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `PathVariable with no value or name should not be changed`() {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(required = false) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewritePathVariable(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(required = false) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @ParameterizedTest
    @ValueSource(strings = ["value", "name"])
    fun `PathVariable containing multiple values with same variable name should be implicit`(input: String) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable($input = "id", required = true) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewritePathVariable(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(required = true) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }
}
