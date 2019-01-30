module search::Search

import ParseTree;
import lang::java::\syntax::Java18;

bool isTest(Tree tree) = /(Annotation)`@Test` := tree;

bool containsAutowired(Tree tree) = /(Annotation)`@Autowired` := tree;

bool hasAllArgsConstructor(Tree tree) = /(Annotation)`@AllArgsConstructor` := tree;
