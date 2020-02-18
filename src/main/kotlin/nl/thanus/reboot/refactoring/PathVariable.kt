package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr

private const val PATH_VARIABLE = "PathVariable"

fun rewritePathVariable(compilationUnit: CompilationUnit) {
    compilationUnit.findAll(Parameter::class.java)
            .forEach { rewritePathVariableName(it) }
}

private fun rewritePathVariableName(parameter: Parameter) {
    when (val annotationExpr = parameter.annotations.firstOrNull { isPathVariableAnnotation(it) }) {
        is SingleMemberAnnotationExpr -> rewriteSinglePathVariable(annotationExpr, parameter)
        is NormalAnnotationExpr -> rewriteNormalPathVariable(annotationExpr, parameter)
    }
}

private fun rewriteSinglePathVariable(annotationExpr: SingleMemberAnnotationExpr, parameter: Parameter) {
    val memberValue = annotationExpr.memberValue

    if (memberValue is StringLiteralExpr) {
        if (memberValue.value == parameter.nameAsString) {
            parameter.annotations
                    .first { isPathVariableAnnotation(it) }
                    .remove()

            parameter.addMarkerAnnotation(PATH_VARIABLE)
        }
    }
}

private fun rewriteNormalPathVariable(annotationExpr: NormalAnnotationExpr, parameter: Parameter) {
    annotationExpr.pairs
            .filter { isSamePathVariableName(it, parameter) }
            .forEach { it.remove() }
}

private fun isSamePathVariableName(it: MemberValuePair, parameter: Parameter) =
        it == MemberValuePair("value", StringLiteralExpr(parameter.name.identifier))

private fun isPathVariableAnnotation(annotation: AnnotationExpr) = annotation.name == Name(PATH_VARIABLE)
