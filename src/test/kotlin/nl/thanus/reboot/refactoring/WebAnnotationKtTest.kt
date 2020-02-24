package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

internal class WebAnnotationKtTest : ReBootBase() {

    @Nested
    @DisplayName("WebAnnotation with only single value")
    inner class SingleMemberAnnotation {

        @ParameterizedTest
        @EnumSource(WebAnnotation::class)
        fun `WebAnnotation with explicit value which is the same as variable name should be implicit`(annotation: WebAnnotation) {
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

            assertRefactored(compilationUnit, expectedCode)
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

            assertRefactored(compilationUnit, expectedCode)
        }
    }

    @Nested
    @DisplayName("WebAnnotation with zero or more values")
    inner class NormalWebAnnotation {

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

            assertRefactored(compilationUnit, expectedCode)
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

            assertRefactored(compilationUnit, expectedCode)
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

            assertRefactored(compilationUnit, expectedCode)
        }

        @ParameterizedTest
        @ValueSource(strings = ["value", "name"])
        fun `PathVariable value should be explicit when it is not the same as variable name`(input: String) {
            val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable($input = "userId") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
            val compilationUnit = StaticJavaParser.parse(code)

            rewriteWebAnnotations(compilationUnit)

            val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable($input = "userId") Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }

        @Test
        fun `PathVariable values with same variable name should be implicit`() {
            val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(name = "id", required = true) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
            val compilationUnit = StaticJavaParser.parse(code)

            rewriteWebAnnotations(compilationUnit)

            val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(required = true) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }

        @Test
        fun `PathVariable values should be explicit when it is not the same as variable name`() {
            val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(name = "userId", required = false) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()
            val compilationUnit = StaticJavaParser.parse(code)

            rewriteWebAnnotations(compilationUnit)

            val expectedCode = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(name = "userId", required = false) Long id) {
                    return ResponseEntity.ok().build();
                }
            }
        """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }
    }

    @Test
    fun `Multiple WebAnnotations with same variable name should be implicit`() {
        val code = """
            public class UsersController {
                public ResponseEntity<User> getUser(@PathVariable(value = "id") Long id, @RequestParam(value = "userName") String userName) {
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

        assertRefactored(compilationUnit, expectedCode)
    }
}
