module \test::search::SearchTest

import ParseTree;
import lang::java::\syntax::Java18;
import search::Search;

test bool shouldBeATest() {
  str testMethod = 
  "@Test
   public void compareTest() {
    assertThat(1, 1);
   }";
  
  return isTest([MethodDeclaration]testMethod);
}

test bool shouldBeNoTest() {
  str method = 
  "public void helloWorld() {
     System.out.println(\"Hello World!\");
   }";
  
  return !isTest([MethodDeclaration]method);
}

test bool shouldContainAutowired() {
  str classWithAutowired = 
  "public class UsersService {
     @Autowired
     private User user;
   }";

  return containsAutowired([ClassDeclaration]classWithAutowired);
}

test bool shouldNotContainAutowired() {
  str classWithAutowired = 
  "public class UsersService {
     private User user;
   }";

  return !containsAutowired([ClassDeclaration]classWithAutowired);
}

test bool shouldContainAllArgs() {
  str classWithAllArgs = 
  "@AllArgsConstructor
   public class UsersService {}";

  return hasAllArgsConstructor([ClassDeclaration]classWithAllArgs);
}

test bool shouldNotContainAllArgs() {
  str classWithoutAllArgs = 
  "public class UsersService {}";

  return !hasAllArgsConstructor([ClassDeclaration]classWithoutAllArgs);
}
