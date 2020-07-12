package nl.thanus.reboot.refactoring

import com.github.javaparser.HasParentNode
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import java.util.Optional

fun <T> HasParentNode<T>.tryAddImportToCompilationUnit(import: String) =
        this.findAncestor(CompilationUnit::class.java).ifPresent { it.addImport(import) }

fun <T> HasParentNode<T>.tryRemoveImportFromCompilationUnit(import: String, isStatic: Boolean = false) =
        this.findAncestor(CompilationUnit::class.java).ifPresent { compilationUnit ->
            compilationUnit.imports
                    .firstOrNull {
                        it == ImportDeclaration(import, isStatic, false)
                    }?.remove()
        }

fun <T> Optional<T>.unwrap(): T? = orElse(null)
