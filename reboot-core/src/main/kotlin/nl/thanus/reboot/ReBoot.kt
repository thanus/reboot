package nl.thanus.reboot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.SourceRoot
import mu.KotlinLogging
import nl.thanus.reboot.refactoring.rewriteAutowiredFieldInjections
import nl.thanus.reboot.refactoring.rewriteMockitoFieldInjections
import nl.thanus.reboot.refactoring.rewriteRequestMappings
import nl.thanus.reboot.refactoring.rewriteWebAnnotations
import java.nio.file.Paths

private val logger = KotlinLogging.logger { }

class ReBoot : CliktCommand() {
    private val location by argument(help = "Location of project that will be ReBooting")
    private val excludedRefactorings: List<Refactoring> by option("--excluded", "-e", help = """
        Refactorings you want ReBoot to exclude, e.g. -e request-mappings -e web-annotations -e autowired-field-injection 
        -e mockito-field-injection
    """.trimIndent()).enum<Refactoring>(key = { it.refactoring }).multiple()

    override fun run() {
        logger.info { "ReBooting $location" }
        parseAndReBoot(location, excludedRefactorings)
        logger.info { "ReBooting completed" }
    }
}

fun main(args: Array<String>) {
    ReBoot().main(args)
}

private fun parseAndReBoot(location: String, excludedRefactorings: List<Refactoring>) {
    val projectRoot = SymbolSolverCollectionStrategy().collect(Paths.get(location))
    val (srcRoot, testRoot) = projectRoot.sourceRoots.partition { it.root.toAbsolutePath().toString().contains("/src/") }

    fun parseSourceRootsAndReBoot(sourceRoots: List<SourceRoot>) {
        sourceRoots.forEach { sourceRoot ->
            sourceRoot.parse("") { _, _, result ->
                val compilationUnit = result.result
                compilationUnit.ifPresent { reboot(it, excludedRefactorings) }

                sourceRoot.setPrinter { LexicalPreservingPrinter.print(it) }
                SourceRoot.Callback.Result.SAVE
            }
        }
    }

    parseSourceRootsAndReBoot(srcRoot)
    parseSourceRootsAndReBoot(testRoot)
}

private fun reboot(compilationUnit: CompilationUnit, excludedRefactorings: List<Refactoring>) {
    LexicalPreservingPrinter.setup(compilationUnit)

    try {
        if (Refactoring.AUTOWIRED_FIELD_INJECTION !in excludedRefactorings) {
            rewriteAutowiredFieldInjections(compilationUnit)
        }
        if (Refactoring.MOCKITO_FIELD_INJECTION !in excludedRefactorings) {
            rewriteMockitoFieldInjections(compilationUnit)
        }
        if (Refactoring.REQUEST_MAPPINGS !in excludedRefactorings) {
            rewriteRequestMappings(compilationUnit)
        }
        if (Refactoring.WEB_ANNOTATIONS !in excludedRefactorings) {
            rewriteWebAnnotations(compilationUnit)
        }
    } catch (e: UnsupportedOperationException) {
        compilationUnit.storage.ifPresent {
            logger.warn { "Lexical preserving failed on ${it.directory}/${it.fileName}" }
            logger.debug(e) { "Lexical preserving failed with ${e.message} on ${it.directory}/${it.fileName}" }
        }
    }
}

enum class Refactoring(val refactoring: String) {
    REQUEST_MAPPINGS("request-mappings"),
    WEB_ANNOTATIONS("web-annotations"),
    AUTOWIRED_FIELD_INJECTION("autowired-field-injection"),
    MOCKITO_FIELD_INJECTION("mockito-field-injection")
}
