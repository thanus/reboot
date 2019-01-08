module refactor::RefactorInjectMocks

import ParseTree;
import lang::java::\syntax::Java15;
import String;
import IO;

Tree refactorInjectMocks(Tree tree) {
  tree = rewriteMockitoImports(tree);
  
  tree = rewriteMockAnnotation(tree);
  tree = rewriteInjectMocksAnnotation(tree);
  tree = rewriteSpyAnnotation(tree);
  
  return tree;
}

Tree rewriteMockitoImports(Tree tree) {
  return innermost visit (tree) {
    case (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* i1>
                          'import org.mockito.Mock;
                          '<ImportDec* i2>
                          '
                          '<TypeDec* t>`
      => (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* i1>
                          'import org.mockito.Mockito;
                          '<ImportDec* i2>
                          '
                          '<TypeDec* t>`
    case (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* ip1>
                          'import org.mockito.Spy;
                          '<ImportDec* ip2>
                          '
                          '<TypeDec* ty>`
      => (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* ip1>
                          'import org.mockito.Mockito;
                          '<ImportDec* ip2>
                          '
                          '<TypeDec* ty>`
    case (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* imp1>
                          'import org.mockito.InjectMocks;
                          '<ImportDec* imp2>
                          '
                          '<TypeDec* td>`
      => (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* imp1>
                          '<ImportDec* imp2>
                          '
                          '<TypeDec* td>`
    case (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* iDec1>
                          'import org.mockito.Mockito;
                          '<ImportDec* iDec2>
                          '
                          '<TypeDec* typ>`
      => (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* iDec1>
                          '<ImportDec* iDec2>
                          '
                          '<TypeDec* typ>`
      when /(ImportDec)`import org.mockito.Mockito;` := iDec1
  }
}

Tree rewriteMockAnnotation(Tree tree) {
  VarInit createMockObject(Type t) = [VarInit]"Mockito.mock(<trim("<t>")>.class)";
  VarInit createMockObjectWithAnswer(Type t, Expr e) = [VarInit]"Mockito.mock(<trim("<t>")>.class, <e>)";
  
  return visit (tree) {
    case (FieldDec)`@Mock <FieldMod f> <Type t><Id i>;`
      => (FieldDec)`<FieldMod f> <Type t><Id i> = <VarInit varInit>;`
      when VarInit varInit := createMockObject(t)
    case (FieldDec)`@Mock <Type t><Id i>;`
      => (FieldDec)`<Type t><Id i> = <VarInit varInit>;`
      when VarInit varInit := createMockObject(t)

    case (FieldDec)`@Mock(answer = <Expr e>) <FieldMod f> <Type t><Id i>;`
      => (FieldDec)`<FieldMod f> <Type t><Id i> = <VarInit varInit>;`
      when VarInit varInit := createMockObjectWithAnswer(t, e)
    case (FieldDec)`@Mock(answer = <Expr e>) <Type t><Id i>;`
      => (FieldDec)`<Type t><Id i> = <VarInit varInit>;`
      when VarInit varInit := createMockObjectWithAnswer(t, e)
 }
}

Tree rewriteSpyAnnotation(Tree tree) {
  VarInit createVarInitWithSpy(VarInit varInit) = [VarInit]"Mockito.spy(<varInit>)";
  
  return visit (tree) {
    case (FieldDec)`@Spy <FieldMod f> <Type t><Id i> = <VarInit varInit>;`
      => (FieldDec)`<FieldMod f> <Type t><Id i> = <VarInit vInit>;`
      when VarInit vInit := createVarInitWithSpy(varInit)
    case (FieldDec)`@Spy <Type t><Id i> = <VarInit varInit>;`
      => (FieldDec)`<Type t><Id i> = <VarInit vInit>;`
      when VarInit vInit := createVarInitWithSpy(varInit)
 }
}

Tree rewriteInjectMocksAnnotation(Tree tree) {
  VarInit createObjectInitialization(Type t) = [VarInit]"new <trim("<t>")>()";
  
  return visit (tree) {
    case (FieldDec)`@InjectMocks <FieldMod f> <Type t><Id i>;`
      => (FieldDec)`<FieldMod f> <Type t><Id i> = <VarInit varInit>;`
      when VarInit varInit := createObjectInitialization(t)
    case (FieldDec)`@InjectMocks <Type t><Id i>;`
      => (FieldDec)`<Type t><Id i> = <VarInit varInit>;`
      when VarInit varInit := createObjectInitialization(t)
  }
}
