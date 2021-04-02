/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Reference extends AST
{
	public Declaration decl;
	public Reference(SourcePosition posn, Declaration decl){
		super(posn);
		if (decl == null) {
	    	// TODO: throw error
	    } else {
	    	this.decl = decl;
	    }
	}

}
