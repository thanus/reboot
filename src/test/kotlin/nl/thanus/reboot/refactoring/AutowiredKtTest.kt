package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AutowiredKtTest : ReBootBase() {

    @Test
    fun `Field injection should be changed to constructor injection`() {
        val code = """
            import org.springframework.beans.factory.annotation.Autowired;
            
            public class UsersController {
                @Autowired
                private UsersService usersService;
            }
        """.trimIndent()

        val compilationUnit = StaticJavaParser.parse(code)

        rewriteAutowiredFieldInjections(compilationUnit)

        val expectedCode = """
            import lombok.AllArgsConstructor;

            @AllArgsConstructor
            public class UsersController {
                private final UsersService usersService;
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `Field injection should not be changed in tests`() {
        val code = """
            import org.junit.jupiter.api.Test;
            import org.springframework.beans.factory.annotation.Autowired;
            
            class UsersControllerTest {
                @Autowired
                private UsersService usersService;

                @Test
                void getUsersTest() {
                    assertEquals(1, 1);
                }
            }
        """.trimIndent()

        val compilationUnit = StaticJavaParser.parse(code)

        rewriteAutowiredFieldInjections(compilationUnit)

        val expectedCode = """
            import org.junit.jupiter.api.Test;
            import org.springframework.beans.factory.annotation.Autowired;
            
            class UsersControllerTest {
                @Autowired
                private UsersService usersService;

                @Test
                void getUsersTest() {
                    assertEquals(1, 1);
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `Should not add constructor when class has a constructor`() {
        val code = """
            import org.springframework.beans.factory.annotation.Autowired;
            
            public class UsersController {
            
                private final UsersService usersService;
                
                @Autowired
                public UsersController(UsersService usersService ) {
                    this.usersService = usersService;
                }
            }
        """.trimIndent()

        val compilationUnit = StaticJavaParser.parse(code)

        rewriteAutowiredFieldInjections(compilationUnit)

        val expectedCode = """
            import org.springframework.beans.factory.annotation.Autowired;
            
            public class UsersController {
            
                private final UsersService usersService;
                
                @Autowired
                public UsersController(UsersService usersService ) {
                    this.usersService = usersService;
                }
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }
}
