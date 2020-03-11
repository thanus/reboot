package nl.thanus.reboot.refactoring

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.NullLiteralExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration
import mu.KotlinLogging
import java.nio.file.Path

private const val MOCK = "Mock"
private const val SPY = "Spy"
private const val INJECT_MOCKS = "InjectMocks"
private const val MOCKITO = "Mockito"

private val logger = KotlinLogging.logger { }

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
    val constructorArguments = resolveAndMatchConstructorArguments(variableDeclarator, fieldDeclaration)

    return NodeList(
            VariableDeclarator(
                    variableDeclarator.type,
                    variableDeclarator.name,
                    ObjectCreationExpr(
                            null,
                            variableDeclarator.type as ClassOrInterfaceType,
                            NodeList(constructorArguments)
                    )
            )
    )
}

private fun resolveAndMatchConstructorArguments(variableDeclarator: VariableDeclarator, fieldDeclaration: FieldDeclaration): List<Expression> {
    val arguments = mutableListOf<Expression>()

    try {
        val fieldDeclarationsInTest = fieldDeclaration.findAncestor(CompilationUnit::class.java)
                .map { it.findAll(FieldDeclaration::class.java) }
                .orElse(emptyList())

        resolveConstructorArguments(variableDeclarator).forEach { type ->
            if (fieldDeclarationsInTest.any { it.elementType == type }) {
                val matchedField = fieldDeclarationsInTest.first { it.elementType == type }
                val variableName = matchedField.variables.first().name
                arguments.add(NameExpr(variableName))
            } else {
                logger.warn { "No matching $type argument found. Adding NullLiteralExpr as constructor argument." }
                arguments.add(NullLiteralExpr())
            }
        }
    } catch (e: UnsolvedSymbolException) {
        val path: Path? = variableDeclarator.findAncestor(CompilationUnit::class.java)
                .flatMap { it.storage }
                .map { it.path }
                .orElse(null)

        logger.warn(e) { "Symbol ${e.name} cannot be resolved. Object creation will not have any constructor arguments. Location: $path" }
    }

    return arguments
}

private fun resolveConstructorArguments(variableDeclarator: VariableDeclarator): List<Type> {
    val typeDeclaration = variableDeclarator.type.resolve().asReferenceType().typeDeclaration

    if (typeDeclaration !is JavaParserClassDeclaration) {
        return emptyList()
    }

    val classOrInterface = typeDeclaration.wrappedNode
    val fields = classOrInterface.findAll(FieldDeclaration::class.java)

    if (fields.any { containsAutowiredAnnotation(it) }) {
        return fields.filter { containsAutowiredAnnotation(it) }.map { it.elementType }
    }

    if (classOrInterface.annotations.any { isAnnotation(it, Name("RequiredArgsConstructor")) }) {
        return fields.filter { isFinalField(it) }.map { it.elementType }
    }

    if (classOrInterface.annotations.any { isAnnotation(it, Name("AllArgsConstructor")) }) {
        return fields.filter { isNonStaticInitializedField(it) }.map { it.elementType }
    }

    val constructors = classOrInterface.constructors
    if (constructors.isNotEmpty()) {
        if (constructors.size == 1) {
            return constructors.first().parameters.map { it.type }
        }

        val parametersType = constructors.firstOrNull { constructor ->
            constructor.annotations.any { isAutowiredAnnotation(it) }
        }?.parameters?.map { it.type }

        return parametersType ?: emptyList()
    }

    return emptyList()
}

fun isFinalField(fieldDeclaration: FieldDeclaration): Boolean {
    val isFinalFields = fieldDeclaration.modifiers.any { it.keyword == Modifier.Keyword.FINAL }
    return isFinalFields && isNonStaticInitializedField(fieldDeclaration)
}

fun isNonStaticInitializedField(fieldDeclaration: FieldDeclaration): Boolean {
    val isNoStaticFields = !fieldDeclaration.modifiers.any { it.keyword == Modifier.Keyword.STATIC }
    val isNoVariableInitializer = !fieldDeclaration.variables.any { it.initializer.isPresent }

    return isNoStaticFields && isNoVariableInitializer
}

private fun addMockitoImportToCompilationUnit(fieldDeclaration: FieldDeclaration) =
        fieldDeclaration.tryAddImportToCompilationUnit("org.mockito.Mockito")

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
