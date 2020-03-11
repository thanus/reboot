package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.Name

fun rewriteAutowiredFieldInjections(compilationUnit: CompilationUnit) {
    if (isTest(compilationUnit)) {
        return
    }

    if (hasConstructor(compilationUnit)) {
        return
    }

    addConstructor(compilationUnit)

    compilationUnit.findAll(FieldDeclaration::class.java)
            .filter { containsAutowiredAnnotation(it) }
            .forEach { removeAutowiredOnFieldAndMakeFinal(it) }
}

private fun addConstructor(compilationUnit: CompilationUnit) {
    val hasAutowiredAnnotation = compilationUnit.findAll(AnnotationExpr::class.java)
            .any { isAutowiredAnnotation(it) }

    if (hasAutowiredAnnotation) {
        compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java)
                .forEach { it.addMarkerAnnotation("RequiredArgsConstructor") }

        compilationUnit.addImport("lombok.RequiredArgsConstructor")
    }
}

private fun hasConstructor(compilationUnit: CompilationUnit): Boolean {
    val hasAllArgsConstructor = compilationUnit.findAll(AnnotationExpr::class.java)
            .any { isLombokConstructorAnnotation(it) }

    val hasConstructor = compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java)
            .any { it.constructors.isNotEmpty() }

    return hasAllArgsConstructor || hasConstructor
}

private fun isTest(compilationUnit: CompilationUnit) =
        compilationUnit.findAll(AnnotationExpr::class.java).any { it.name == Name("Test") }

private fun isLombokConstructorAnnotation(annotationExpr: AnnotationExpr) =
        annotationExpr.name == Name("RequiredArgsConstructor") || annotationExpr.name == Name("AllArgsConstructor")

fun containsAutowiredAnnotation(fieldDeclaration: FieldDeclaration) =
        fieldDeclaration.annotations.any { isAutowiredAnnotation(it) }

fun isAutowiredAnnotation(annotationExpr: AnnotationExpr) = annotationExpr.name == Name("Autowired")

private fun removeAutowiredOnFieldAndMakeFinal(fieldDeclaration: FieldDeclaration) {
    fieldDeclaration.tryRemoveImportFromCompilationUnit("org.springframework.beans.factory.annotation.Autowired")

    fieldDeclaration.annotations
            .first { isAutowiredAnnotation(it) }
            .remove()

    fieldDeclaration.isFinal = true
}
