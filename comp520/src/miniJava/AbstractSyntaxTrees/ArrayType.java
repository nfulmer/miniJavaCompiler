/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

/* adding 'length' 
 * field to miniJava - add length to ArrayType
 * syntactically, something should be able to say 
 * array.length 
 * so we add to expressions and maybe also references? 
 * but I think with references, they can be assigned like ref = expr;
 * we can't assign array.length, only use so it should be an expression
 * then for each of the visitation classes, we have to add a visit 
 * array length method
 * so I think we want ArrayLengthExpr that extends Expression and has a reference
 */

/*
 * ok so steps
 * 1) need `length` token for scanner
 * 2) need `ArrayLengthExpr` node for parser (that is made up of a reference) + expand parse rules for expression
 * 3) need ArrayLengthExpr visitation method for
 * --> ASTDisplay: ?
 * --> Identification: verify that reference has been declared + assign it the right declarations
 * --> TypeChecking: verify that reference is array type
 * ?) add length field to ArrayType TypeDenoter then in assignment statements, add capacity to change that? (will need to read int value from new Array() expression
 */

public class ArrayType extends TypeDenoter {
		public int length;
		
		public ArrayType(TypeDenoter eltType, SourcePosition posn, int length){
	        super(TypeKind.ARRAY, posn);
	        this.eltType = eltType;
	        this.length = length;
	    }

	    public ArrayType(TypeDenoter eltType, SourcePosition posn){
	        super(TypeKind.ARRAY, posn);
	        this.eltType = eltType;
	        this.length = 0;
	    }
	        
	    public <A,R> R visit(Visitor<A,R> v, A o) {
	        return v.visitArrayType(this, o);
	    }

	    public TypeDenoter eltType;
	}

