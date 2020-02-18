package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType

private const val MOCK = "Mock"
private const val SPY = "Spy"
private const val INJECT_MOCKS = "InjectMocks"
private const val MOCKITO = "Mockito"

fun rewriteMockitoFieldInjections(compilationUnit: CompilationUnit) {
    compilationUnit.findAll(FieldDeclaration::class.java)
            .filter { fieldDeclaration -> fieldDeclaration.annotations.any { isMockitoAnnotation(it) } }
            .forEach { rewriteMockitoAnnotations(it) }

    removeMockitoFieldInjectionImports(compilationUnit.imports)
}

private fun rewriteMockitoAnnotations(fieldDeclaration: FieldDeclaration) {
    val mockitoAnnotation = fieldDeclaration.annotations.first { isMockitoAnnotation(it) }

    when (mockitoAnnotation.name) {
        Name(MOCK) -> rewriteAnnotation(fieldDeclaration, mockitoAnnotation.name, ::assignMockObject)
        Name(SPY) -> rewriteAnnotation(fieldDeclaration, mockitoAnnotation.name, ::assignSpyObject)
        Name(INJECT_MOCKS) -> rewriteAnnotation(fieldDeclaration, mockitoAnnotation.name, ::instantiateObject)
    }
}

private fun assignMockObject(fieldDeclaration: FieldDeclaration): NodeList<VariableDeclarator> {
    addMockitoImportToCompilationUnit(fieldDeclaration)
    val variableDeclarator = fieldDeclaration.variables.first()

    return NodeList(
            VariableDeclarator(
                    variableDeclarator.type,
                    variableDeclarator.name,
                    MethodCallExpr(
                            NameExpr(MOCKITO),
                            MOCK.toLowerCase(),
                            NodeList<Expression>(ClassExpr(variableDeclarator.type))
                    )
            )
    )
}

private fun assignSpyObject(fieldDeclaration: FieldDeclaration): NodeList<VariableDeclarator> {
    addMockitoImportToCompilationUnit(fieldDeclaration)
    val variableDeclarator = fieldDeclaration.variables.first()

    return NodeList(
            VariableDeclarator(
                    variableDeclarator.type,
                    variableDeclarator.name,
                    MethodCallExpr(
                            NameExpr(MOCKITO),
                            SPY.toLowerCase(),
                            NodeList<Expression>(
                                    ObjectCreationExpr(
                                            null,
                                            variableDeclarator.type as ClassOrInterfaceType,
                                            NodeList()
                                    )
                            )
                    )
            )
    )
}

private fun rewriteAnnotation(fieldDeclaration: FieldDeclaration, mockitoAnnotation: Name, rewriteVariable: (FieldDeclaration) -> NodeList<VariableDeclarator>) {
    fieldDeclaration.annotations.first { isAnnotation(it, mockitoAnnotation) }.remove()
    fieldDeclaration.variables = rewriteVariable(fieldDeclaration)
}

private fun instantiateObject(fieldDeclaration: FieldDeclaration): NodeList<VariableDeclarator> {
    val variableDeclarator = fieldDeclaration.variables.first()

    return NodeList(
            VariableDeclarator(
                    variableDeclarator.type,
                    variableDeclarator.name,
                    ObjectCreationExpr(
                            null,
                            variableDeclarator.type as ClassOrInterfaceType,
                            NodeList()
                    )
            )
    )
}

private fun addMockitoImportToCompilationUnit(fieldDeclaration: FieldDeclaration) =
        fieldDeclaration.findAncestor(CompilationUnit::class.java).ifPresent { it.addImport("org.mockito.Mockito") }

private fun isMockitoAnnotation(annotation: AnnotationExpr) =
        setOf(Name(INJECT_MOCKS), Name(MOCK), Name(SPY)).any { isAnnotation(annotation, it) }

private fun isAnnotation(annotation: AnnotationExpr, name: Name) = annotation.name == name

private fun removeMockitoFieldInjectionImports(imports: NodeList<ImportDeclaration>) {
    val mockitoImports = setOf(
            ImportDeclaration("org.mockito.InjectMocks", false, false),
            ImportDeclaration("org.mockito.Mock", false, false),
            ImportDeclaration("org.mockito.Spy", false, false)
    )

    imports.filter { mockitoImports.contains(it) }.forEach { it.remove() }
}
