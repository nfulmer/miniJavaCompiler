/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import  miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;

public class ClassDecl extends Declaration {
	
  public ClassDecl(String cn, FieldDeclList fdl, MethodDeclList mdl, SourcePosition posn) {
	  // class declaration has type class???
	  // changed from:
	  //super(cn, null, posn);
	  super(cn, new ClassType(new Identifier(new Token(TokenKind.ID, cn, posn)), posn), posn);

	  /*
	  if (cn.equals("String")) {
		  super(cn, new BaseType(TypeKind.UNSUPPORTED, posn), posn);
	  } */
	  fieldDeclList = fdl;
	  methodDeclList = mdl;
  }
  
  public <A,R> R visit(Visitor<A, R> v, A o) {
      return v.visitClassDecl(this, o);
  }
      
  public FieldDeclList fieldDeclList;
  public MethodDeclList methodDeclList;
}
