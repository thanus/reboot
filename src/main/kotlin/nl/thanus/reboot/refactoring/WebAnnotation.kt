package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr

enum class WebAnnotation {
    PathVariable, RequestParam, RequestHeader, RequestAttribute, CookieValue, ModelAttribute, SessionAttribute
}

const val NAME = "name"
const val VALUE = "value"

fun rewriteWebAnnotations(compilationUnit: CompilationUnit) {
    compilationUnit.findAll(Parameter::class.java)
            .forEach { rewriteWebAnnotationName(it) }
}

private fun rewriteWebAnnotationName(parameter: Parameter) {
    val annotationExpr = parameter.annotations.filter { isWebAnnotation(it) }

    annotationExpr.forEach {
        when (it) {
            is SingleMemberAnnotationExpr -> rewriteSingleWebAnnotation(it, parameter)
            is NormalAnnotationExpr -> rewriteNormalWebAnnotation(it, parameter)
        }
    }
}

private fun rewriteSingleWebAnnotation(annotationExpr: SingleMemberAnnotationExpr, parameter: Parameter) {
    val memberValue = annotationExpr.memberValue

    if (memberValue is StringLiteralExpr && memberValue.value == parameter.nameAsString) {
        replaceWebAnnotationWithMarkerAnnotation(parameter)
    }
}

private fun rewriteNormalWebAnnotation(annotationExpr: NormalAnnotationExpr, parameter: Parameter) {
    if (containsOnlyNameOrValue(annotationExpr.pairs)) {
        replaceWebAnnotationWithMarkerAnnotation(parameter)
        return
    }

    annotationExpr.pairs
            .filter { isWebAnnotationSameAsVariableName(it, parameter) }
            .forEach { it.remove() }
}

private fun replaceWebAnnotationWithMarkerAnnotation(parameter: Parameter) {
    val annotation = parameter.annotations.first { isWebAnnotation(it) }
    annotation.remove()

    parameter.addMarkerAnnotation(annotation.name.identifier)
}

private fun containsOnlyNameOrValue(pairs: List<MemberValuePair>) = pairs.any { isNameOrValue(it) } && pairs.size == 1

private fun isNameOrValue(it: MemberValuePair) = it.name == SimpleName(NAME) || it.name == SimpleName(VALUE)

private fun isWebAnnotationSameAsVariableName(pair: MemberValuePair, parameter: Parameter) =
        when (pair.name) {
            SimpleName(VALUE) -> pair == MemberValuePair(pair.name, StringLiteralExpr(parameter.name.identifier))
            SimpleName(NAME) -> pair == MemberValuePair(pair.name, StringLiteralExpr(parameter.name.identifier))
            else -> false
        }

private fun isWebAnnotation(annotation: AnnotationExpr) = WebAnnotation.values().any { it.name == annotation.name.identifier }
