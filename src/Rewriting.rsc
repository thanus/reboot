module Rewriting

import Prelude;
import ParseTree;
import lang::java::\syntax::Java15;
import util::DirectoryTraverser;
import util::Parse;

public void main(str arg) {
  loc project = |file:///| + arg;
  checkIfLocationExistsAndIsDirectory(project);
}
