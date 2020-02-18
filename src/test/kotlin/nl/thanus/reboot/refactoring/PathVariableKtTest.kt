package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
    fun `PathVariable containing multiple values with same variable name should be implicit`() {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(value = "id", required = true) Long id) {
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
