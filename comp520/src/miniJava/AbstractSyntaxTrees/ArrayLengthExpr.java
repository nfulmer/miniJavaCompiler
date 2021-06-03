package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

// added for PA4

public class ArrayLengthExpr extends FieldDecl {
	
	public Reference r;

	// STATIC CHECKS!!!
	
	public ArrayLengthExpr(Reference r, SourcePosition posn) {
		// TODO: is arraylength initialized as 0?
		super(false, false, new BaseType(TypeKind.INT, posn), "length", null, posn);
		this.r = r;
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		// TODO Auto-generated method stub
		return null;
	}

}
