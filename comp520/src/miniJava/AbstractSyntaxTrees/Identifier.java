/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;

public class Identifier extends Terminal {
	public Declaration decl;
	
  public Identifier (Token t) {
    super (t);
    /*
    if (decl == null) {
    	// TODO: throw error
    } else {
    	this.decl = decl;
    }*/
 
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitIdentifier(this, o);
  }

}
