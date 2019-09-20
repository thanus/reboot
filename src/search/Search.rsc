module search::Search

import ParseTree;
import lang::java::\syntax::Java18;

public bool isTest(Tree tree) = /(Annotation)`@Test` := tree;

public bool containsAutowired(Tree tree) = /(Annotation)`@Autowired` := tree;

public bool hasAllArgsConstructor(Tree tree) = /(Annotation)`@AllArgsConstructor` := tree;
