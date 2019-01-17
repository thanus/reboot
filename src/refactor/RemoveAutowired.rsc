module refactor::RemoveAutowired

import ParseTree;
import lang::java::\syntax::Java18;
import String;

import search::Search;

Tree refactorFieldInjectionToConstructor(Tree tree) {
  bool isNoTest = !isTest(tree);
  
  tree = removeAutowiredImportAndAllArgs(tree, isNoTest);
  tree = addConstructorWhenNotSpecified(tree, isNoTest);
  tree = removeAutowiredOnFields(tree, isNoTest);
  
  return tree;
}

Tree removeAutowiredImportAndAllArgs(Tree tree, bool isNoTest) {
  return innermost visit (tree) {
    case (Annotation)`@AllArgsConstructor(onConstructor = @__(@Autowired))` => (Annotation)`@AllArgsConstructor`
    case (Imports)`<ImportDeclaration* i1>
                  'import org.springframework.beans.factory.annotation.Autowired;
                  '<ImportDeclaration* i2>`
      => (Imports)`<ImportDeclaration* i1>
                  '<ImportDeclaration* i2>`
      when isNoTest
  }
}

Tree addConstructorWhenNotSpecified(Tree tree, bool isNoTest) {
  bool needsConstructor(tree) = !containsAutowired(tree) && isNoTest;
  
  return visit (tree) {
    case (NormalClassDeclaration)`<ClassModifier* cm> class <Identifier i> <TypeParameters? t><Superclass? su><Superinterfaces? sInf><ClassBody cb>`
      => (NormalClassDeclaration)`@AllArgsConstructor
                                 '<ClassModifier* cm> class <Identifier i> <TypeParameters? t><Superclass? su><Superinterfaces? sInf><ClassBody cb>`
      when needsConstructor(tree)
    
    case (Imports)`<ImportDeclaration* i1>
                  '<ImportDeclaration* i2>`
      => (Imports)`<ImportDeclaration* i1>
                  'import lombok.AllArgsConstructor;
                  '<ImportDeclaration* i2>`
      when needsConstructor(tree)
  }
}

Tree removeAutowiredOnFields(Tree tree, bool isNoTest) {
  return visit (tree) {
    case (FieldDeclaration)`@Autowired <FieldModifier* f> <UnannType t><VariableDeclaratorId i>;`
      => (FieldDeclaration)`<FieldModifier* f> final <UnannType t><VariableDeclaratorId i>;`
      when isNoTest
    case (FieldDeclaration)`@Autowired <UnannType t><VariableDeclaratorId i>;`
      => (FieldDeclaration)`final <UnannType t><VariableDeclaratorId i>;`
      when isNoTest
  }
}
