package nl.thanus.reboot

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.MissingParameter
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThatThrownBy

internal class ReBootKtTest {

    @Test
    fun `Directory should always be specified in the CLI`() {
        val emptyCliArgs = arrayOf<String>()
        assertThatThrownBy { ReBoot().parse(emptyCliArgs) }.isExactlyInstanceOf(MissingParameter::class.java)
    }

    @Test
    fun `Excluding refactorings is optional`() {
        val cliArgs = arrayOf("directory")
        assertThatCode { ReBoot().parse(cliArgs) }.doesNotThrowAnyException()
    }

    @Test
    fun `Excluding refactorings with flag -e and --excluded is accepted`() {
        val cliArgs = arrayOf(
                "differentDirectory",
                "-e", Refactoring.REQUEST_MAPPINGS.refactoring,
                "--excluded", Refactoring.AUTOWIRED_FIELD_INJECTION.refactoring
        )
        assertThatCode { ReBoot().parse(cliArgs) }.doesNotThrowAnyException()
    }

    @Test
    fun `Excluding non-existent refactoring is not allowed`() {
        val cliArgs = arrayOf("directory", "-e", "nothing")
        assertThatThrownBy { ReBoot().parse(cliArgs) }.isExactlyInstanceOf(BadParameterValue::class.java)
    }
}
