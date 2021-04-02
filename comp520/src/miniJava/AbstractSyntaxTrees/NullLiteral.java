package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;

//added class

public class NullLiteral extends Terminal {

	public NullLiteral(Token t) {
		super(t);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitNullLiteral(this, o);
	}

}
