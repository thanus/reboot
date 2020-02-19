package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.SimpleName
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

    if (memberValue is StringLiteralExpr && memberValue.value == parameter.nameAsString) {
        replacePathVariableWithMarkerAnnotation(parameter)
    }
}

private fun rewriteNormalPathVariable(annotationExpr: NormalAnnotationExpr, parameter: Parameter) {
    if (containsOnlyNameOrValue(annotationExpr.pairs)) {
        replacePathVariableWithMarkerAnnotation(parameter)
        return
    }

    annotationExpr.pairs
            .filter { isPathVariableSameAsVariableName(it, parameter) }
            .forEach { it.remove() }
}

private fun replacePathVariableWithMarkerAnnotation(parameter: Parameter) {
    parameter.annotations
            .first { isPathVariableAnnotation(it) }
            .remove()

    parameter.addMarkerAnnotation(PATH_VARIABLE)
}

private fun containsOnlyNameOrValue(pairs: List<MemberValuePair>) = pairs.any { isNameOrValue(it) } && pairs.size == 1
private fun isNameOrValue(it: MemberValuePair) = it.name == SimpleName("name") || it.name == SimpleName("value")

private fun isPathVariableSameAsVariableName(pair: MemberValuePair, parameter: Parameter) =
        when (pair.name) {
            SimpleName("value") -> pair == MemberValuePair(pair.name, StringLiteralExpr(parameter.name.identifier))
            SimpleName("name") -> pair == MemberValuePair(pair.name, StringLiteralExpr(parameter.name.identifier))
            else -> false
        }

private fun isPathVariableAnnotation(annotation: AnnotationExpr) = annotation.name == Name(PATH_VARIABLE)
