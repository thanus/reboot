package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class WebAnnotationKtTest : ReBootBase() {

    @ParameterizedTest
    @EnumSource(WebAnnotation::class)
    fun `WebAnnotation value with same variable name should be implicit`(annotation: WebAnnotation) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation("id") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteWebAnnotations(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation Long id) {
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

        rewriteWebAnnotations(compilationUnit)

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
    @EnumSource(WebAnnotation::class)
    fun `WebAnnotation with explicit value which is the same as variable name should be implicit`(annotation: WebAnnotation) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation(value = "id") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteWebAnnotations(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @ParameterizedTest
    @EnumSource(WebAnnotation::class)
    fun `WebAnnotation with explicit name which is the same as variable name should be implicit`(annotation: WebAnnotation) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation(name = "id") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteWebAnnotations(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @ParameterizedTest
    @EnumSource(WebAnnotation::class)
    fun `WebAnnotation with no value or name should not be changed`(annotation: WebAnnotation) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation(required = false) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteWebAnnotations(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation(required = false) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @ParameterizedTest
    @EnumSource(WebAnnotation::class)
    fun `WebAnnotation containing multiple values with same variable name should be implicit`(annotation: WebAnnotation) {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation(name = "id", required = true) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteWebAnnotations(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@$annotation(required = true) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `WebAnnotations with same variable name should be implicit`() {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(value = "id") Long id, @RequestParam(value = "userNam") String userName) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteWebAnnotations(compilationUnit)

        val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable Long id, @RequestParam String userName) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }
}
