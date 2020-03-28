package nl.thanus.reboot

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

fun main(args: Array<String>) {
    val location = args.firstOrNull()
    location?.let {
        logger.info { "ReBooting $it" }
        parseAndReBoot(it)
        logger.info { "ReBooting completed" }
    }
}

private fun parseAndReBoot(location: String) {
    val projectRoot = SymbolSolverCollectionStrategy().collect(Paths.get(location))
    val (srcRoot, testRoot) = projectRoot.sourceRoots.partition { it.root.toAbsolutePath().toString().contains("/src/") }

    fun parseSourceRootsAndReBoot(sourceRoots: List<SourceRoot>) {
        sourceRoots.forEach { sourceRoot ->
            sourceRoot.parse("") { _, _, result ->
                val compilationUnit = result.result
                compilationUnit.ifPresent { reboot(it) }

                sourceRoot.setPrinter { LexicalPreservingPrinter.print(it) }
                SourceRoot.Callback.Result.SAVE
            }
        }
    }

    parseSourceRootsAndReBoot(srcRoot)
    parseSourceRootsAndReBoot(testRoot)
}

private fun reboot(compilationUnit: CompilationUnit) {
    LexicalPreservingPrinter.setup(compilationUnit)

    try {
        rewriteAutowiredFieldInjections(compilationUnit)
        rewriteMockitoFieldInjections(compilationUnit)
        rewriteRequestMappings(compilationUnit)
        rewriteWebAnnotations(compilationUnit)
    } catch (e: UnsupportedOperationException) {
        compilationUnit.storage.ifPresent {
            logger.warn { "Lexical preserving failed on ${it.directory}/${it.fileName}" }
            logger.debug(e) { "Lexical preserving failed with ${e.message} on ${it.directory}/${it.fileName}" }
        }
    }
}
