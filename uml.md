### Current UML Model (Major Classes)
```
		   Node
			|
			|
	--------|        -----RDFLiteral
	|	   	|	     |
	|		|	     |
	|    RDFNode ----|
	|		| 	     |
	|	 	|        |
    |		|	     -----RDFGraph  	
	|		|
	|	 RDFResource
	|				     									
	---------------------------------------------------------------------------------------------------------				     						
		|		|		|			|			|		|
	     TermMap	     TripleMap	    PredicateObjectMap	     LogicalSource	      Graph	     DataSource
		|
	------------------
   GraphMap  ObjectMap  SubjectMap
	    FunctionMap

```


### Current UML of the big three (Uri, BlankNode, Literal): 

```
					Node 
					 |
					 |
					Entity
					 |
					 |
			---------------------------------
			|				                |
		      TermNode			   ExplicitNode
		-----------------		-----------------
		|		|		        |		        |
	   Uri   BlankNode          |	        Literal
		|				        |
		-------------------------

```


