module \test::refactor::RemoveAutowiredTest

import ParseTree;
import lang::java::\syntax::Java18;
import refactor::RemoveAutowired;

test bool shouldRemoveAutowiredFromAllArgs() {
  Tree allArgsWithAutowired = [Annotation]"@AllArgsConstructor(onConstructor = @__(@Autowired))";
  Tree rewrittenTree = refactorFieldInjectionToConstructor(allArgsWithAutowired);
  
  Tree allArgsWithoutAutowired = [Annotation]"@AllArgsConstructor";
  
  return rewrittenTree == allArgsWithoutAutowired;
}

test bool shouldRemoveAutowiredImport() {
  str imports = 
  "import lombok.AllArgsConstructor;
  'import org.springframework.beans.factory.annotation.Autowired;";
  
  Tree rewrittenTree = refactorFieldInjectionToConstructor([Imports]imports);
  
  Tree importsWithoutAutowired = [Imports]"import lombok.AllArgsConstructor;";
  
  return rewrittenTree == importsWithoutAutowired;
}

test bool shouldRewriteFieldInjectionToConstructorInjection() {
  Tree classWithFieldInjection = [CompilationUnit]
  "import org.springframework.beans.factory.annotation.Autowired;
  '
  'public class UsersService {
  '  @Autowired private User user;
  '}";
  
  Tree rewrittenTree = refactorFieldInjectionToConstructor(classWithFieldInjection);
  
  Tree classWithConstructorInjection = [CompilationUnit]
  "import lombok.AllArgsConstructor;
  '
  '@AllArgsConstructor
  'public class UsersService {
  '  private final User user;
  '}";
  
  return "<rewrittenTree>" == "<classWithConstructorInjection>";
}

test bool shouldRewriteFieldInjectionWithoutFieldModifierToConstructorInjection() {
  Tree classWithFieldInjection = [CompilationUnit]
  "import org.springframework.beans.factory.annotation.Autowired;
  '
  'public class UserService {
  '  @Autowired User user;
  '}";
  
  Tree rewrittenTree = refactorFieldInjectionToConstructor(classWithFieldInjection);
  
  Tree classWithConstructorInjection = [CompilationUnit]
  "import lombok.AllArgsConstructor;
  '
  '@AllArgsConstructor
  'public class UserService {
  '  final User user;
  '}";
  
  return "<rewrittenTree>" == "<classWithConstructorInjection>";
}

test bool shouldNotRewriteFieldInjectionInUnitTest() {
  Tree unitTestWithFieldInjection = [CompilationUnit]
  "import org.springframework.beans.factory.annotation.Autowired;
  '
  'public class UserService {
  '  @Autowired
  '  private User user;
  '
  '  @Test
  '  public void testSomething() {
  '    assertThat(1, 1);
  '  }
  '}";
  
  Tree rewrittenTree = refactorFieldInjectionToConstructor(unitTestWithFieldInjection);

  return rewrittenTree == unitTestWithFieldInjection;
}
