package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MockitoKtTest : ReBootBase() {

    @Test
    fun `Rewrite @Mock including import`() {
        val code = """
            import org.mockito.Mock;
            
            class UsersControllerTest {
                @Mock
                private UsersService usersService;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                private UsersService usersService = Mockito.mock(UsersService.class);
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `Rewrite @Spy including import`() {
        val code = """
            import org.mockito.Spy;
            
            class UsersControllerTest {
                @Spy
                private UsersService usersService;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                private UsersService usersService = Mockito.spy(new UsersService());
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `Rewrite @InjectMocks with object creation`() {
        val code = """
            import org.mockito.InjectMocks;
            
            class UsersControllerTest {
                @InjectMocks
                private UsersController usersController;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            class UsersControllerTest {
                private UsersController usersController = new UsersController();
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }

    @Test
    fun `Should not add Mockito import when there is already one`() {
        val code = """
            import org.mockito.Mock;
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                @Mock
                private UsersService usersService;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                private UsersService usersService = Mockito.mock(UsersService.class);
            }
        """.trimIndent()

        assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
    }
}
