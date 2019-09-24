module refactor::RemoveAutowired

import ParseTree;
import lang::java::\syntax::Java18;
import String;

import search::Search;

public Tree refactorFieldInjectionToConstructor(Tree tree) {
  if (isTest(tree)) {
    return tree;
  }
  
  tree = removeAutowiredImportAndAllArgs(tree);
  tree = addConstructorWhenNotSpecified(tree);
  tree = removeAutowiredOnFields(tree);
  
  return tree;
}

Tree removeAutowiredImportAndAllArgs(Tree tree) {
  return innermost visit (tree) {
    case (Annotation)`@AllArgsConstructor(onConstructor = @__(@Autowired))` => (Annotation)`@AllArgsConstructor`
    case (Imports)`<ImportDeclaration* i1>
                  'import org.springframework.beans.factory.annotation.Autowired;
                  '<ImportDeclaration* i2>`
      => (Imports)`<ImportDeclaration* i1>
                  '<ImportDeclaration* i2>`
  }
}

Tree addConstructorWhenNotSpecified(Tree tree) {
  bool needsConstructor = containsAutowired(tree) && !hasAllArgsConstructor(tree);
  
  return visit (tree) {
    case (NormalClassDeclaration)`<ClassModifier* cm> class <Identifier i> <TypeParameters? t><Superclass? su><Superinterfaces? sInf><ClassBody cb>`
      => (NormalClassDeclaration)`@AllArgsConstructor
                                 '<ClassModifier* cm> class <Identifier i> <TypeParameters? t><Superclass? su><Superinterfaces? sInf><ClassBody cb>`
      when needsConstructor
    
    case (Imports)`<ImportDeclaration* i1>
                  '<ImportDeclaration* i2>`
      => (Imports)`<ImportDeclaration* i1>
                  'import lombok.AllArgsConstructor;
                  '<ImportDeclaration* i2>`
      when needsConstructor
  }
}

Tree removeAutowiredOnFields(Tree tree) {
  return visit (tree) {
    case (FieldDeclaration)`@Autowired <FieldModifier* f> <UnannType t><VariableDeclaratorId i>;`
      => (FieldDeclaration)`<FieldModifier* f> final <UnannType t><VariableDeclaratorId i>;`
    case (FieldDeclaration)`@Autowired <UnannType t><VariableDeclaratorId i>;`
      => (FieldDeclaration)`final <UnannType t><VariableDeclaratorId i>;`
  }
}
