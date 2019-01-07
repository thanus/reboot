module refactor::RemoveAutowired

import ParseTree;
import lang::java::\syntax::Java15;

Tree removeAutowired(Tree tree) {
  bool isNoTest = !isTest(tree);
  
  tree = innermost visit (tree) {
    case (Anno)`@AllArgsConstructor(onConstructor = @__(@Autowired))` => (Anno)`@AllArgsConstructor`
    case (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* i1>
                          'import org.springframework.beans.factory.annotation.Autowired;
                          '<ImportDec* i2>
                          '
                          '<TypeDec* t>`
      => (CompilationUnit)`<PackageDec? p>
                          '
                          '<ImportDec* i1>
                          '<ImportDec* i2>
                          '
                          '<TypeDec* t>`
      when isNoTest
  }

  tree = innermost visit (tree) {
    case (ClassBody)`{
                    '  <ClassBodyDec* cs1>
                    '  @Autowired
                    '  <FieldMod f> <Type t><Id id>;
                    '  <ClassBodyDec* cs2>
                    '}`
      => (ClassBody)`{
                    '  <ClassBodyDec* cs1>
                    '  <FieldMod f> final <Type t><Id id>;
                    '  <ClassBodyDec* cs2>
                    '}`
      when isNoTest
    case (ClassBody)`{
                    '  <ClassBodyDec* cb1>
                    '  @Autowired
                    '  <Type t><Id id>;
                    '  <ClassBodyDec* cb2>
                    '}`
      => (ClassBody)`{
                    '  <ClassBodyDec* cb1>
                    '  final <Type t><Id id>;
                    '  <ClassBodyDec* cb2>
                    '}`
      when isNoTest
    
  }
  
  return tree;
}

bool isTest(Tree tree) = /(Anno)`@Test` := tree;
