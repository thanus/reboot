module refactor::RemoveAutowired

import ParseTree;
import lang::java::\syntax::Java15;

Tree removeAutowired(Tree tree) {
  return visit (tree) {
    //case (ImportDec)`<ImportDec i>` => (ImportDec)`<ImportDec i>`
    //case (ImportDec)`import org.springframework.beans.factory.annotation.Autowired;` => 
    //  (ImportDec)`import org.springframework.beans.factory.annotation.Autowired;`
    
    //case (CompilationUnit)`<PackageDec? p>
    //                      '
    //                      '<ImportDec* i>
    //                      '
    //                      '<TypeDec* t>`
    //  => (CompilationUnit)`<PackageDec? p>
    //                      '
    //                      '<ImportDec* i>
    //                      '
    //                      '<TypeDec* t>`         
    
    case (Anno)`@AllArgsConstructor(onConstructor = @__(@Autowired))` => (Anno)`@AllArgsConstructor`
  }
}
