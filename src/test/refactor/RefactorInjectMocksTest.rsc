module \test::refactor::RefactorInjectMocksTest

import ParseTree;
import lang::java::\syntax::Java18;
import refactor::RefactorInjectMocks;

test bool shouldRewriteMockImport() {
  str imports =
  "import org.springframework.boot.SpringApplication;
  'import org.springframework.boot.autoconfigure.SpringBootApplication;
  'import org.mockito.Mock;";
  
  Tree rewrittenTree = refactorInjectMocks([Imports]imports);
  
  Tree importsWithOnlyMockImport = [Imports]
  "import org.springframework.boot.SpringApplication;
  'import org.springframework.boot.autoconfigure.SpringBootApplication;
  'import org.mockito.Mockito;";
  
  return rewrittenTree == importsWithOnlyMockImport;
}

test bool shouldOnlyContainOneMockitoImport() {
  str imports =
  "import org.springframework.boot.SpringApplication;
  'import org.springframework.boot.autoconfigure.SpringBootApplication;
  'import org.mockito.Mock;
  'import org.mockito.Spy;
  'import org.mockito.InjectMocks;";
  
  Tree rewrittenTree = refactorInjectMocks([Imports]imports);
  
  Tree importsWithOnlyOneMockitoImport = [Imports]
  "import org.springframework.boot.SpringApplication;
  'import org.springframework.boot.autoconfigure.SpringBootApplication;
  'import org.mockito.Mockito;";
  
  return rewrittenTree == importsWithOnlyOneMockitoImport;
}

test bool shouldRewriteMockAnnotation() {
  Tree fieldWithMockAnnotation = [FieldDeclaration]"@Mock private A a;";
  Tree rewrittenTree = refactorInjectMocks(fieldWithMockAnnotation);
  
  Tree fieldWithoutMockAnnotation = [FieldDeclaration]"private A a = Mockito.mock(A.class);";
  
  return rewrittenTree == fieldWithoutMockAnnotation;
}

test bool shouldRewriteMockAnnotationWithoutFieldModifier() {
  Tree fieldWithMockAnnotation = [FieldDeclaration]"@Mock A a;";
  Tree rewrittenTree = refactorInjectMocks(fieldWithMockAnnotation);
  
  Tree fieldWithoutMockAnnotation = [FieldDeclaration]"A a = Mockito.mock(A.class);";
  
  return rewrittenTree == fieldWithoutMockAnnotation;
}

test bool shouldRewriteMockAnswerAnnotation() {
  Tree fieldWithMockAnnotation = [FieldDeclaration]"@Mock(answer = Answers.RETURNS_DEEP_STUBS) private A a;";
  Tree rewrittenTree = refactorInjectMocks(fieldWithMockAnnotation);
  
  Tree fieldWithoutMockAnnotation = [FieldDeclaration]"private A a = Mockito.mock(A.class, Answers.RETURNS_DEEP_STUBS);";
  
  return rewrittenTree == fieldWithoutMockAnnotation;
}

test bool shouldRewriteMockAnswerAnnotationWithoutFieldModifier() {
  Tree fieldWithMockAnnotation = [FieldDeclaration]"@Mock(answer = Answers.RETURNS_DEEP_STUBS) A a;";
  Tree rewrittenTree = refactorInjectMocks(fieldWithMockAnnotation);
  
  Tree fieldWithoutMockAnnotation = [FieldDeclaration]"A a = Mockito.mock(A.class, Answers.RETURNS_DEEP_STUBS);";
  
  return rewrittenTree == fieldWithoutMockAnnotation;
}

test bool shouldRewriteSpyAnnotation() {
  Tree fieldWithSpyAnnotation = [FieldDeclaration]"@Spy private A a = new A();";
  Tree rewrittenTree = refactorInjectMocks(fieldWithSpyAnnotation);
  
  Tree fieldWithoutSpyAnnotation = [FieldDeclaration]"private A a = Mockito.spy(new A());";
  
  return rewrittenTree == fieldWithoutSpyAnnotation;
}

test bool shouldRewriteSpyAnnotationWithoutFieldModifier() {
  Tree fieldWithSpyAnnotation = [FieldDeclaration]"@Spy A a = new A();";
  Tree rewrittenTree = refactorInjectMocks(fieldWithSpyAnnotation);
  
  Tree fieldWithoutSpyAnnotation = [FieldDeclaration]"A a = Mockito.spy(new A());";
  
  return rewrittenTree == fieldWithoutSpyAnnotation;
}

test bool shouldRewriteInjectMocksAnnotation() {
  Tree fieldWithInjectMocksAnnotation = [FieldDeclaration]"@InjectMocks private A a;";
  Tree rewrittenTree = refactorInjectMocks(fieldWithInjectMocksAnnotation);
  
  Tree fieldWithoutInjectMockAnnotation = [FieldDeclaration]"private A a = new A();";
  
  return rewrittenTree == fieldWithoutInjectMockAnnotation;
}

test bool shouldRewriteInjectMocksAnnotationWithoutFieldModifier() {
  Tree fieldWithInjectMocksAnnotation = [FieldDeclaration]"@InjectMocks A a;";
  Tree rewrittenTree = refactorInjectMocks(fieldWithInjectMocksAnnotation);
  
  Tree fieldWithoutInjectMockAnnotation = [FieldDeclaration]"A a = new A();";
  
  return rewrittenTree == fieldWithoutInjectMockAnnotation;
}
