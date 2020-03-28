package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun rewriteRequestMappings(compilationUnit: CompilationUnit) {
    compilationUnit.findAll(MethodDeclaration::class.java)
            .filter { containsRequestMappingAnnotation(it) }
            .forEach { rewriteRequestMappingAnnotations(it) }
}

private fun rewriteRequestMappingAnnotations(method: MethodDeclaration) {
    val requestMappingAnnotation = method.annotations.first { isRequestMapping(it) } as NormalAnnotationExpr
    val pairs = requestMappingAnnotation.pairs

    val requestMethod: SimpleName? = when (val value = pairs.first { it.name == SimpleName("method") }.value) {
        is NameExpr -> value.name
        is FieldAccessExpr -> value.name
        else -> {
            logger.warn { "Unknown RequestMethod expression: $requestMappingAnnotation" }
            null
        }
    }

    when (requestMethod) {
        SimpleName("GET") -> rewriteRequestMapping(method, pairs, "GetMapping")
        SimpleName("POST") -> rewriteRequestMapping(method, pairs, "PostMapping")
        SimpleName("PUT") -> rewriteRequestMapping(method, pairs, "PutMapping")
        SimpleName("PATCH") -> rewriteRequestMapping(method, pairs, "PatchMapping")
        SimpleName("DELETE") -> rewriteRequestMapping(method, pairs, "DeleteMapping")
    }
}

private fun containsRequestMappingAnnotation(method: MethodDeclaration) = method.annotations.any { isRequestMapping(it) }

private fun rewriteRequestMapping(method: MethodDeclaration, pairs: NodeList<MemberValuePair>, annotation: String) {
    method.annotations.first { isRequestMapping(it) }.remove()
    optimizeImports(method, annotation)

    if (pairs.size == 1) {
        method.addMarkerAnnotation(annotation)
        return
    }

    val pairsWithoutRequestMethod = getPairsWithoutRequestMethod(pairs)

    if (containsOnlyPathOrValue(pairsWithoutRequestMethod)) {
        val pathOrValue = pairsWithoutRequestMethod.first { isPathOrValue(it) }
        method.addAnnotation(SingleMemberAnnotationExpr(Name(annotation), pathOrValue.value))
        return
    }

    method.addAnnotation(NormalAnnotationExpr(Name(annotation), NodeList(pairsWithoutRequestMethod)))
}

private fun isRequestMapping(it: AnnotationExpr) = it.name == Name("RequestMapping")

private fun getPairsWithoutRequestMethod(pairs: NodeList<MemberValuePair>) =
        pairs.filterNot { it.name == SimpleName("method") }

private fun containsOnlyPathOrValue(pairs: List<MemberValuePair>) = pairs.any { isPathOrValue(it) } && pairs.size == 1
private fun isPathOrValue(it: MemberValuePair) = it.name == SimpleName("path") || it.name == SimpleName("value")

private fun optimizeImports(method: MethodDeclaration, annotation: String) {
    method.tryAddImportToCompilationUnit("org.springframework.web.bind.annotation.$annotation")

    val requestMethod = annotation.substring(0, annotation.length - 7).toUpperCase()
    method.tryRemoveImportFromCompilationUnit("org.springframework.web.bind.annotation.RequestMethod")
    method.tryRemoveImportFromCompilationUnit("org.springframework.web.bind.annotation.RequestMethod.$requestMethod", true)

    val sharedRequestMapping = method.findAncestor(ClassOrInterfaceDeclaration::class.java)
            .map { it.annotations }
            .orElse(NodeList(emptyList<AnnotationExpr>()))
            .any { isRequestMapping(it) }

    if (!sharedRequestMapping) {
        method.tryRemoveImportFromCompilationUnit("org.springframework.web.bind.annotation.RequestMapping")
    }
}
