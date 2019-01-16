module refactor::RemoveAutowired

import ParseTree;
import lang::java::\syntax::Java18;

import search::Search;

Tree removeAutowired(Tree tree) {
  bool isNoTest = !isTest(tree);
  
  tree = innermost visit (tree) {
    case (Annotation)`@AllArgsConstructor(onConstructor = @__(@Autowired))` => (Annotation)`@AllArgsConstructor`
    case (Imports)`<ImportDeclaration* i1>
                  'import org.springframework.beans.factory.annotation.Autowired;
                  '<ImportDeclaration* i2>`
      => (Imports)`<ImportDeclaration* i1>
                  '<ImportDeclaration* i2>`
      when isNoTest
  }

  tree = visit (tree) {
    case (FieldDeclaration)`@Autowired <FieldModifier* f> <UnannType t><VariableDeclaratorId i>;`
      => (FieldDeclaration)`<FieldModifier* f> final <UnannType t><VariableDeclaratorId i>;`
      when isNoTest
    case (FieldDeclaration)`@Autowired <UnannType t><VariableDeclaratorId i>;`
      => (FieldDeclaration)`final <UnannType t><VariableDeclaratorId i>;`
      when isNoTest
  }
  
  return tree;
}
