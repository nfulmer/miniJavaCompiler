package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

// added for PA4

public class ArrayLengthExpr extends Expression {
	
	public Reference r;

	public ArrayLengthExpr(Reference r, SourcePosition posn) {
		super(posn);
		this.r = r;
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		// TODO Auto-generated method stub
		return null;
	}

}
