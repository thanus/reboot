module search::Search

import ParseTree;
import lang::java::\syntax::Java15;

bool isTest(Tree tree) = /(Anno)`@Test` := tree;