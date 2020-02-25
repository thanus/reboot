package nl.thanus.reboot.refactoring

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import org.assertj.core.api.Assertions.assertThat

open class ReBootBase {
    companion object {
        init {
            StaticJavaParser.setConfiguration(
                    ParserConfiguration().setLexicalPreservationEnabled(true)
            )
        }
    }
}

fun assertRefactored(compilationUnit: CompilationUnit, expectedCode: String) {
    assertThat(LexicalPreservingPrinter.print(compilationUnit)).isEqualTo(expectedCode)
}
