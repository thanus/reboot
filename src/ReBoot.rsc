module ReBoot

import Prelude;
import ParseTree;
import lang::java::\syntax::Java18;

import refactor::RemoveAutowired;
import refactor::RefactorInjectMocks;

import util::DirectoryTraverser;
import util::Parse;

public void main(str arg) {
  datetime startTime = now();
  
  loc project = |file:///| + arg;
  checkIfLocationExistsAndIsDirectory(project);
  
  rewrite(project);
  
  Duration duration = now() - startTime;
  println("ReBoot completed in <duration>");
}

void rewrite(loc project) {
  list[loc] sourceFiles = getAllFilesFor(project);
  list[Tree] parseTrees = parse(sourceFiles);
  
  for (Tree parseTree <- parseTrees) {
    Tree tree = refactorFieldInjectionToConstructor(parseTree);
    tree = refactorInjectMocks(tree);
    
    writeFile(parseTree@\loc, tree);
  }
}
