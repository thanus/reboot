package nl.thanus.reboot.refactoring

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser

open class ReBootBase {
    companion object {
        init {
            StaticJavaParser.setConfiguration(
                    ParserConfiguration().setLexicalPreservationEnabled(true)
            )
        }
    }
}
