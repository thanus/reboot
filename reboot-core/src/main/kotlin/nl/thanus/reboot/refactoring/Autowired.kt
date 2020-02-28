package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.ThisExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ExpressionStmt

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
        addConstructorToClassesWithAutowired(compilationUnit)
//        compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java)
//                .forEach { it.addMarkerAnnotation("RequiredArgsConstructor") }

//        compilationUnit.addImport("lombok.RequiredArgsConstructor")
    }
}

private fun addConstructorToClassesWithAutowired(compilationUnit: CompilationUnit) {
    compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java)
            .forEach { classOrInterfaceDeclaration ->
                val fields = classOrInterfaceDeclaration.fields.filter { containsAutowiredAnnotation(it) }

                val parameters = fields.map { Parameter(it.elementType, it.variables.first().name) }
                val statements = fields.map {
                    ExpressionStmt(
                            AssignExpr(
                                    FieldAccessExpr(ThisExpr(), it.variables.first().name.identifier),
                                    NameExpr(SimpleName(it.variables.first().name.identifier)),
                                    AssignExpr.Operator.ASSIGN
                            )
                    )
                }

                val constructor = classOrInterfaceDeclaration.addConstructor(Modifier.Keyword.PUBLIC)
                constructor.parameters = NodeList(parameters)
                constructor.body = BlockStmt(NodeList(statements))
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

private fun containsAutowiredAnnotation(fieldDeclaration: FieldDeclaration) =
        fieldDeclaration.annotations.any { isAutowiredAnnotation(it) }

private fun isAutowiredAnnotation(annotationExpr: AnnotationExpr) = annotationExpr.name == Name("Autowired")

private fun removeAutowiredOnFieldAndMakeFinal(fieldDeclaration: FieldDeclaration) {
    fieldDeclaration.tryRemoveImportFromCompilationUnit("org.springframework.beans.factory.annotation.Autowired")

    fieldDeclaration.annotations
            .first { isAutowiredAnnotation(it) }
            .remove()

    fieldDeclaration.isFinal = true
}
