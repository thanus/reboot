module util::Parse

import IO;
import List;
import ParseTree;
import lang::java::\syntax::Java18;

list[Tree] parse(list[loc] sourceFiles) {
  list[Tree] parseTrees = [];
  
  list[loc] ambFiles = [];
  list[loc] parseErrorFiles = [];
  
  for (loc file <- sourceFiles) {
    try {
      Tree parseTree = parse(#start[CompilationUnit], file, allowAmbiguity = true);
      
      if(/t:amb(_) := parseTree) {
        ambFiles += file;
      }
      
      parseTrees += parseTree;
      
    } catch ParseError(loc parseError): {
      parseErrorFiles += parseError;
    }
  }
  
  println("Parsed:          <size(parseTrees)>");  
  println("Amb:             <size(ambFiles)>");
  println("Parse failures:  <size(parseErrorFiles)>");
  
  return parseTrees;
}
