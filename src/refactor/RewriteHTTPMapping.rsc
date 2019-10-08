module refactor::RewriteHTTPMapping

import ParseTree;
import lang::java::\syntax::Java18;
import String;
import Prelude;

public Tree refactorWithExplicitHTTPMapping(Tree tree) {
  return visit (tree) {
    case (MethodModifier)`@RequestMapping(<ElementValuePairList elements>)`: {
      
      // Rewrite GET RequestMapping
      if (/(ElementValuePair)`method = RequestMethod.GET` := elements) {
	    // Rewrite to Annotation without elements when there is only a RequestMethod element
	    if (countElementValuePairs(elements) == 1) {
	      insert (MethodModifier)`@GetMapping`;
	      break;
	    }
	    
	    ElementValuePairList filteredElementValuePairs = filterElementValuePairListOnGet(elements);
	    
	    // Rewrite to Annotation with path/value when elements contains only path/value
	    if (countElementValuePairs(filteredElementValuePairs) == 1) {
	      ElementValue pathValue = getPathOrValueFrom(filteredElementValuePairs);
	      insert (MethodModifier)`@GetMapping(<ElementValuePairList pathValue>)`;
	      break;
	    }
	    
	    // Rewrite to Annotation with multiple elements
	    insert (MethodModifier)`@GetMapping(<ElementValuePairList filteredElementValuePairs>)`;
	  }
	  
	  // Rewrite PUT RequestMapping
	  if (/(ElementValuePair)`method = RequestMethod.PUT` := elements) {
	    if (countElementValuePairs(elements) == 1) {
	      insert (MethodModifier)`@PutMapping`;
	      break;
	    }
	    
	    ElementValuePairList filteredElementValuePairs = filterElementValuePairListOnPut(elements);
	    
	    // Rewrite to Annotation with path/value when elements contains only path/value
	    if (countElementValuePairs(filteredElementValuePairs) == 1) {
	      ElementValue pathValue = getPathOrValueFrom(filteredElementValuePairs);
	      insert (MethodModifier)`@PutMapping(<ElementValuePairList pathValue>)`;
	      break;
	    }
	    
	    // Rewrite to Annotation with multiple elements
	    insert (MethodModifier)`@PutMapping(<ElementValuePairList filteredElementValuePairs>)`;
	  }
	  
	  // Rewrite POST RequestMapping
	  if (/(ElementValuePair)`method = RequestMethod.POST` := elements) {
	    if (countElementValuePairs(elements) == 1) {
	      insert (MethodModifier)`@PostMapping`;
	      break;
	    }
	    
	    ElementValuePairList filteredElementValuePairs = filterElementValuePairListOnPost(elements);
	    
	    // Rewrite to Annotation with path/value when elements contains only path/value
	    if (countElementValuePairs(filteredElementValuePairs) == 1) {
	      ElementValue pathValue = getPathOrValueFrom(filteredElementValuePairs);
	      insert (MethodModifier)`@PostMapping(<ElementValuePairList pathValue>)`;
	      break;
	    }
	    
	    // Rewrite to Annotation with multiple elements
	    insert (MethodModifier)`@PostMapping(<ElementValuePairList filteredElementValuePairs>)`;
	  }
	  
	  // Rewrite DELETE RequestMapping
	  if (/(ElementValuePair)`method = RequestMethod.DELETE` := elements) {
	    if (countElementValuePairs(elements) == 1) {
	      insert (MethodModifier)`@DeleteMapping`;
	      break;
	    }
	    
	    ElementValuePairList filteredElementValuePairs = filterElementValuePairListOnDelete(elements);
	    
	    // Rewrite to Annotation with path/value when elements contains only path/value
	    if (countElementValuePairs(filteredElementValuePairs) == 1) {
	      ElementValue pathValue = getPathOrValueFrom(filteredElementValuePairs);
	      insert (MethodModifier)`@DeleteMapping(<ElementValuePairList pathValue>)`;
	      break;
	    }
	    
	    // Rewrite to Annotation with multiple elements
	    insert (MethodModifier)`@DeleteMapping(<ElementValuePairList filteredElementValuePairs>)`;
	  }
	  
	  // Rewrite PATCH RequestMapping
	  if (/(ElementValuePair)`method = RequestMethod.PATCH` := elements) {
	    if (countElementValuePairs(elements) == 1) {
	      insert (MethodModifier)`@PatchMapping`;
	      break;
	    }
	    
	    ElementValuePairList filteredElementValuePairs = filterElementValuePairListOnPatch(elements);
	    
	    // Rewrite to Annotation with path/value when elements contains only path/value
	    if (countElementValuePairs(filteredElementValuePairs) == 1) {
	      ElementValue pathValue = getPathOrValueFrom(filteredElementValuePairs);
	      insert (MethodModifier)`@PatchMapping(<ElementValuePairList pathValue>)`;
	      break;
	    }
	    
	    // Rewrite to Annotation with multiple elements
	    insert (MethodModifier)`@PatchMapping(<ElementValuePairList filteredElementValuePairs>)`;
	  }
	}
  }
}

ElementValuePairList filterElementValuePairListOnGet(elements) = filterElementValuePairListOn(elements, (ElementValuePair)`method = RequestMethod.GET`);
ElementValuePairList filterElementValuePairListOnPut(elements) = filterElementValuePairListOn(elements, (ElementValuePair)`method = RequestMethod.PUT`);
ElementValuePairList filterElementValuePairListOnPost(elements) = filterElementValuePairListOn(elements, (ElementValuePair)`method = RequestMethod.POST`);
ElementValuePairList filterElementValuePairListOnDelete(elements) = filterElementValuePairListOn(elements, (ElementValuePair)`method = RequestMethod.DELETE`);
ElementValuePairList filterElementValuePairListOnPatch(elements) = filterElementValuePairListOn(elements, (ElementValuePair)`method = RequestMethod.PATCH`);

int countElementValuePairs(ElementValuePairList elementList) {
  int count = 0;
  visit (elementList) {
    case (ElementValuePair)`<Identifier id> = <ElementValue val>`: count += 1;
  }
  return count;
}

ElementValue getPathOrValueFrom(ElementValuePairList elementList) {
  return top-down-break visit (elementList) {
    case (ElementValuePair)`path = <ElementValue path>`: { 
      return path;
    }
    case (ElementValuePair)`value = <ElementValue val>`: { 
      return val;
    }
  }
}

ElementValuePairList filterElementValuePairListOn(ElementValuePairList elementList, ElementValuePair filterElement) {
  list[ElementValuePair] elements = [];
  
  visit (elementList) {
    case ElementValuePair element : {
      if (element != filterElement) {
        elements = elements + element;
      }
    }
  }
  
  str filteredElementList = intercalate(", ", elements);
  return [ElementValuePairList]filteredElementList;
}
