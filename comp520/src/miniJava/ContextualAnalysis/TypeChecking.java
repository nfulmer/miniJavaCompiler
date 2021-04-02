package miniJava.ContextualAnalysis;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ContextualAnalysis.Identification.SyntaxError;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;

public class TypeChecking implements Visitor<String, Object> {
	
	private ErrorReporter reporter;
	private AST ast;
	private MethodDecl curMeth;

	public TypeChecking(AST ast, ErrorReporter reporter) {
		// TODO Auto-generated constructor stub
		this.reporter = reporter;
		this.ast = ast;
		
		//predefined classes??
		visitPackage((Package) ast, "");
	}

	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;
	}
	
	private void typeError (String e) throws SyntaxError{
		reporter.reportError(e);
		//throw new SyntaxError();
	}
	
	boolean equals(TypeDenoter t1, TypeDenoter t2, SourcePosition posn) {
		// TODO: verify
		
		if (t1.typeKind.equals(TypeKind.UNSUPPORTED) || t2.typeKind.equals(TypeKind.UNSUPPORTED)) {
			typeError("*** line " + String.valueOf(posn.getPosition()) + ": type mismatch between " + t2.typeKind.toString() + " and " + t1.typeKind.toString() + "!");
			return false;
		} 
		
		if (t1.typeKind.equals(TypeKind.ARRAY)) {
				// have to check that array is the right type
			if (((ArrayType) t1).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
				typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type  " + ((ArrayType) t1).eltType.typeKind.toString() + "!");
				return false;
			}
		}
		if (t2.typeKind.equals(TypeKind.ARRAY)) {
			// have to check that array is the right type
			if (((ArrayType) t2).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
				typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type " + ((ArrayType) t2).eltType.typeKind.toString() + "!");				
				return false;
			}
		}
		if (t1.typeKind.equals(TypeKind.ERROR) || t2.typeKind.equals(TypeKind.ERROR)) {
			return true;
		}
		if (!t1.typeKind.equals(t2.typeKind)) {
			typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + t2.typeKind.toString() + " and " + t1.typeKind.toString() + "!");
			return false;
		} else {
			if (t1.typeKind.equals(TypeKind.ARRAY)) {
				// have to check that array is the right type
				if (!t2.typeKind.equals(TypeKind.ARRAY)) {
					typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + t2.typeKind.toString() + " and " +  t1.typeKind.toString() + "!");
					return false;
				}
				if (!((ArrayType) t1).eltType.typeKind.equals(((ArrayType) t2).eltType.typeKind)){
					typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + ((ArrayType) t2).eltType.typeKind.toString() + " and " + ((ArrayType) t1).eltType.typeKind.toString() + "!");
					return false;
				} else if (((ArrayType) t1).eltType.typeKind.equals(TypeKind.UNSUPPORTED) || ((ArrayType) t1).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
					typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + ((ArrayType) t2).eltType.typeKind.toString() + " and " + ((ArrayType) t1).eltType.typeKind.toString() + "!");
					return false;
				}
				else if (((ArrayType) t1).eltType.typeKind.equals(TypeKind.CLASS)){

					if (!((ArrayType) t2).eltType.typeKind.equals(TypeKind.CLASS)) {
						typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + ((ArrayType) t2).eltType.toString() + " and " + ((ClassType) ((ArrayType) t1).eltType).className.spelling + "!");
						return false;
					}

					if (!((ClassType) ((ArrayType) t1).eltType).className.spelling.equals(((ClassType) ((ArrayType) t2).eltType).className.spelling)) {
						typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + ((ClassType) ((ArrayType) t2).eltType).className.spelling + " and " + ((ClassType) ((ArrayType) t1).eltType).className.spelling + "!");
						return false;
					}
				}
			} else if (t1.typeKind.equals(TypeKind.CLASS)) {
				if(!((ClassType) t1).className.spelling.equals(((ClassType) t2).className.spelling)){
					typeError("*** line " + String.valueOf(posn.getPosition()) + ": Type Error: type mismatch " + ((ClassType) t2).className.spelling + " and " + ((ClassType) t1).className.spelling + "!");
					return false;
				}
			}
		}
		return true;
	}
	
	/*VOID,
		INT,
        BOOLEAN,
        CLASS,
        ARRAY,
        
        //added NULL
        NULL,
        
        UNSUPPORTED,
        ERROR; */
	boolean okOper(TypeKind tk, Operator o) {
		if (tk.equals(TypeKind.ERROR)) {
			return true;
		} else if (tk.equals(TypeKind.UNSUPPORTED)) {
			return false;
		}
		if (o.spelling == "-") {
			if (!tk.equals(TypeKind.INT)) {
				return false;
			}
		} else if (o.spelling == "!") {
			if (!tk.equals(TypeKind.BOOLEAN)) {
				return false;
			}
		}
		return true;
	}
	
	/* operators:
	 * 
	 * disjunction ||
		conjunction &&
		equality ==, !=
		relational <=, <, >, >=
		additive +, -
		multiplicative *, /
		unary -, !
	 */
	
	boolean okOper(TypeDenoter t1, TypeDenoter t2, Operator o, SourcePosition sp) {
		if (t1.typeKind.equals(TypeKind.UNSUPPORTED) || t1.typeKind.equals(TypeKind.UNSUPPORTED)) {
			typeError("*** line " + String.valueOf(sp.getPosition()) + ": type mismatch between " + t2.typeKind.toString() + " and " + t1.typeKind.toString() + "!");
			return false;
		}
		if (t1.typeKind.equals(TypeKind.ARRAY)) {
			// have to check that array is the right type
		if (((ArrayType) t1).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
			typeError("*** line " + String.valueOf(sp.getPosition()) + ": type mismatch between " + t2.typeKind.toString() + " and " + ((ArrayType) t1).eltType.typeKind.toString() + "!");
			return false;
		}
		}
		if (t2.typeKind.equals(TypeKind.ARRAY)) {
			// have to check that array is the right type
			if (((ArrayType) t2).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
				typeError("*** line " + String.valueOf(sp.getPosition()) + ": type mismatch between " + t1.typeKind.toString() + " and " + ((ArrayType) t2).eltType.typeKind.toString() + "!");
				return false;
			}
		}
		// error is always fine, unsupported is not fine
		// used to be && 
		if (t1.typeKind.equals(TypeKind.ERROR) || t2.typeKind.equals(TypeKind.ERROR)) {
			return true;
		}
		if ((o.spelling == "==" || o.spelling == "!=") && (t1.typeKind.equals(TypeKind.NULL) || t2.typeKind.equals(TypeKind.NULL))) {
			// null can be legally tested for (in)equality against any object
			return true;
		}
		if (o.spelling == "||" || o.spelling == "&&") {
			if(!(t1.typeKind.equals(TypeKind.ERROR) || t1.typeKind.equals(TypeKind.BOOLEAN))) {
				typeError("*** line " + String.valueOf(sp.getPosition()) + ": cannot apply operator " + o.spelling + " to type " + t1.typeKind.toString() + "!");
				return false;
			}
			// if not error or boolean
			if(!(t2.typeKind.equals(TypeKind.ERROR) || t2.typeKind.equals(TypeKind.BOOLEAN))) {
				typeError("*** line " + String.valueOf(sp.getPosition()) + ": cannot apply operator " + o.spelling + " to type " + t2.typeKind.toString() + "!");
				return false;
			}
			return true;
		}
		if (o.spelling == "<=" || o.spelling == ">=" || o.spelling == ">" || o.spelling == "<"
				|| o.spelling == "+" || o.spelling == "-" || o.spelling == "*" || o.spelling == "/") {
			if(!(t1.typeKind.equals(TypeKind.ERROR) || t1.typeKind.equals(TypeKind.INT))) {
				typeError("*** line " + String.valueOf(sp.getPosition()) + ": cannot apply operator " + o.spelling + " to type " + t1.typeKind.toString() + "!");
				return false;
			}
			if(!(t2.typeKind.equals(TypeKind.ERROR) || t2.typeKind.equals(TypeKind.INT))) {
				typeError("*** line " + String.valueOf(sp.getPosition()) + ": cannot apply operator " + o.spelling + " to type " + t2.typeKind.toString() + "!");
				return false;
			}
			return true;
		}
		if (o.spelling == "==" || o.spelling == "!=") {
			// null can be legally tested for (in)equality against any object
			return equals(t1, t2, sp);
		}
		return false;
	}
	
	TypeKind result(Operator o) {
		if (o.spelling == "+" || o.spelling == "-" || o.spelling == "*" || o.spelling == "/") {
			return TypeKind.INT;
		} else if (o.spelling == "<=" || o.spelling == ">=" || o.spelling == ">" || o.spelling == "<" 
				|| o.spelling == "==" || o.spelling == "!=" || o.spelling == "!" 
				|| o.spelling == "||" || o.spelling == "&&" ) {
			return TypeKind.BOOLEAN;
		} else {
			return TypeKind.UNSUPPORTED;
		}
	}

	@Override
	public Object visitPackage(Package prog, String arg) {
		for (ClassDecl c : prog.classDeclList) {
			visitClassDecl(c, arg);
		}
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, String arg) {
		// don't need to visit the fields??
		
		for (MethodDecl md : cd.methodDeclList) {
			curMeth = md;
			visitMethodDecl(md, arg);
		}
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, String arg) {
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, String arg) {
		for (Statement s: md.statementList) {
			visitStatement(s, arg);
		}
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, String arg) {
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, String arg) {
		if (decl.type.typeKind.equals(TypeKind.CLASS)) {
			return ((ClassType) decl.type).className.decl.type;
		}
		return decl.type;
	}

	@Override
	public Object visitBaseType(BaseType type, String arg) {
		
		return type;
	}

	@Override
	public Object visitClassType(ClassType type, String arg) {
		return type.className.decl.type;
	}

	@Override
	public Object visitArrayType(ArrayType type, String arg) {
		// TODO verify??
		return type;
	}

	public Object visitStatement(Statement s, String arg) throws SyntaxError{
		if (s instanceof BlockStmt) {
			visitBlockStmt((BlockStmt) s, arg);
		} else if (s instanceof VarDeclStmt) {
			visitVardeclStmt((VarDeclStmt) s, arg);
		} else if (s instanceof AssignStmt) {
			visitAssignStmt((AssignStmt) s, arg);
		} else if (s instanceof IxAssignStmt) {
			visitIxAssignStmt((IxAssignStmt) s, arg);
		} else if (s instanceof CallStmt) {
			visitCallStmt((CallStmt) s, arg);
		} else if (s instanceof ReturnStmt) {
			visitReturnStmt((ReturnStmt) s, arg);
		} else if (s instanceof IfStmt) {
			visitIfStmt((IfStmt) s, arg);
		} else if (s instanceof WhileStmt) {
			visitWhileStmt((WhileStmt) s, arg);
		} else {
			typeError("*** line " + String.valueOf(s.posn.getPosition()) + ": Statement not recognized as particular type!");
		}
		
		return null;
	}
	
	@Override
	public Object visitBlockStmt(BlockStmt stmt, String arg) {
		for (Statement s: stmt.sl) {
			visitStatement(s, arg);
		}
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, String arg) {
		TypeDenoter declaration = (TypeDenoter) visitVarDecl(stmt.varDecl, arg);
		TypeDenoter assignExpr = (TypeDenoter) visitExpression(stmt.initExp, arg);
		
		boolean unsupport = false;
		if (declaration.typeKind.equals(TypeKind.UNSUPPORTED)) {
			unsupport = true;
		} else if (declaration.typeKind.equals(TypeKind.ARRAY) && ((ArrayType) declaration).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
			unsupport = true;
		}
		
		if (assignExpr.typeKind.equals(TypeKind.NULL) && !unsupport) {
			return null;
		}
		
		
		equals(declaration, assignExpr, stmt.posn);
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, String arg) {
		TypeDenoter var = (TypeDenoter) visitReference(stmt.ref, arg);
		TypeDenoter expr = (TypeDenoter) visitExpression(stmt.val, arg);
		
		boolean unsupport = false;
		if (var.typeKind.equals(TypeKind.UNSUPPORTED)) {
			unsupport = true;
		} else if (var.typeKind.equals(TypeKind.ARRAY) && ((ArrayType) var).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
			unsupport = true;
		}
		
		if (expr.typeKind.equals(TypeKind.NULL) && !unsupport) {
			return null;
		}
		
		equals(var, expr, stmt.posn);
		return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, String arg) {
		TypeKind arrayIndex = ((TypeDenoter) visitExpression(stmt.ix, arg)).typeKind;
		if (!arrayIndex.equals(TypeKind.INT)) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: cannot index array with expression of type " + arrayIndex.toString() + "!");
		}
		// need cases int[] banana = new int[?];
		// and banana[0] = 2;
		
		
		if (stmt.ref.decl instanceof ClassDecl || stmt.ref.decl instanceof MethodDecl) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: cannot index class or method!");
		} else {
		TypeDenoter ref = (TypeDenoter) visitReference(stmt.ref, arg);
		TypeDenoter expr = (TypeDenoter) visitExpression(stmt.exp, arg);
		if (!ref.typeKind.equals(TypeKind.ARRAY)) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: cannot index variable of type " + ref.typeKind.toString() + "!");
		} else {
			if (expr.typeKind.equals(TypeKind.NULL) && !((ArrayType) ref).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
				return null;
			}
			equals(((ArrayType) ref).eltType, expr, stmt.posn);
		}}
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, String arg) {
		TypeDenoter decl;
		TypeDenoter exprT;
		if (!(stmt.methodRef.decl instanceof MethodDecl)) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: trying to make a call on a non-method!");
		} else {
		if (((MethodDecl) stmt.methodRef.decl).parameterDeclList.size() != stmt.argList.size()) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": there are " + String.valueOf(stmt.argList.size()) + " arguments entered for a method that takes " + String.valueOf(((MethodDecl) stmt.methodRef.decl).parameterDeclList.size()) + "!");
		} else {
		for (int i = 0; i < ((MethodDecl) stmt.methodRef.decl).parameterDeclList.size(); i++) {
			decl = ((MethodDecl) stmt.methodRef.decl).parameterDeclList.get(i).type;
			exprT = (TypeDenoter) visitExpression(stmt.argList.get(i), arg);
			if (!exprT.typeKind.equals(TypeKind.NULL)) {
				equals(decl, exprT, stmt.posn);
			}
		}
		} }
		return null;

	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, String arg) {
		if (stmt.returnExpr == null) {
			if (!curMeth.type.typeKind.equals(TypeKind.VOID)) {
				typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: method needs to return type of " + curMeth.type.typeKind.toString() + " but no return expression!");
			}
		} else {
			if (curMeth.type.typeKind.equals(TypeKind.VOID) && stmt.returnExpr != null) {
				typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: void method can't return anything!");
			} else {
				TypeDenoter val = (TypeDenoter) visitExpression(stmt.returnExpr, arg);
				TypeDenoter methodDecl = curMeth.type;
				equals(methodDecl, val, stmt.posn);
			}
		}
		
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, String arg) {
		TypeKind condition = ((TypeDenoter) visitExpression(stmt.cond, arg)).typeKind;
		if (!condition.equals(TypeKind.BOOLEAN)) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: condition needs type BOOLEAN but recieved type " + condition.toString() + "!");
		}
		visitStatement(stmt.thenStmt, arg);
		if (stmt.elseStmt != null) {
			visitStatement(stmt.elseStmt, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, String arg) {
		TypeKind condition = ((TypeDenoter) visitExpression(stmt.cond, arg)).typeKind;
		if (!condition.equals(TypeKind.BOOLEAN)) {
			typeError("*** line " + String.valueOf(stmt.posn.getPosition()) + ": Type Error: condition needs type BOOLEAN but recieved type " + condition.toString() + "!");
		}
		visitStatement(stmt.body, arg);
		return null;
	}

	Object visitExpression(Expression e, String arg) throws SyntaxError{
		if (e instanceof UnaryExpr) {
			return visitUnaryExpr((UnaryExpr) e, arg);
		} else if (e instanceof BinaryExpr) {
			return visitBinaryExpr((BinaryExpr) e, arg);
		} else if (e instanceof RefExpr) {
			return visitRefExpr((RefExpr) e, arg);
		} else if (e instanceof IxExpr) {
			return visitIxExpr((IxExpr) e, arg);
		} else if (e instanceof CallExpr) {
			return visitCallExpr((CallExpr) e, arg);
		} else if (e instanceof LiteralExpr) {
			return visitLiteralExpr((LiteralExpr) e, arg);
		} else if (e instanceof NewObjectExpr) {
			return visitNewObjectExpr((NewObjectExpr) e, arg);
		} else if (e instanceof NewArrayExpr) {
			return visitNewArrayExpr((NewArrayExpr) e, arg);
		} else {
			typeError("*** line " + String.valueOf(e.posn.getPosition()) + ": Expression not recognized type!");
		}
		
		return null;
	} 
	
	@Override
	public Object visitUnaryExpr(UnaryExpr expr, String arg) {
		TypeDenoter expression = (TypeDenoter) visitExpression(expr.expr, arg);
		if (!okOper(expression.typeKind, expr.operator)) {
			typeError("*** line " + String.valueOf(expr.posn.getPosition()) + ": Type Error: cannot apply operator " + expr.operator.spelling + " to type " + expression.typeKind.toString() + "!");
		}
		return new BaseType(result(expr.operator), expr.posn);
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, String arg) {
		TypeDenoter e1 = (TypeDenoter) visitExpression(expr.left, arg);
		TypeDenoter e2 = (TypeDenoter) visitExpression(expr.right, arg);

		okOper(e1, e2, expr.operator, expr.posn);
		return new BaseType(result(expr.operator), expr.posn);
	}

	@Override
	public Object visitRefExpr(RefExpr expr, String arg) {
		//System.out.println(expr.ref.decl.type.typeKind);
		if (expr.ref.decl.type.typeKind.equals(TypeKind.CLASS)) {
			return ((ClassType) expr.ref.decl.type).className.decl.type;
		} else {
			return expr.ref.decl.type;
		}
	}

	@Override
	public Object visitIxExpr(IxExpr expr, String arg) {
		// TODO: class type??
		TypeKind arrayIndex = ((TypeDenoter) visitExpression(expr.ixExpr, arg)).typeKind;
		if (!arrayIndex.equals(TypeKind.INT)) {
			typeError("*** line " + String.valueOf(expr.posn.getPosition()) + ": Type Error: cannot index array with expression of type " + arrayIndex.toString() + "!");
		}
		if (!expr.ref.decl.type.typeKind.equals(TypeKind.ARRAY)) {
			typeError("*** line " + String.valueOf(expr.posn.getPosition()) + ": Type Error: cannot index variable of type " + expr.ref.decl.type.typeKind.toString() + "!");
			return new BaseType(TypeKind.ERROR, expr.posn);
		} else {
			if (((ArrayType) expr.ref.decl.type).eltType.typeKind.equals(TypeKind.CLASS)) {
				return ((ClassType) ((ArrayType) expr.ref.decl.type).eltType).className.decl.type;
			}
			return ((ArrayType) expr.ref.decl.type).eltType;
		}
		
	}

	@Override
	public Object visitCallExpr(CallExpr expr, String arg) {
		// TODO class type
		TypeDenoter decl;
		TypeDenoter exprT;
		if (!(expr.functionRef.decl instanceof MethodDecl)) {
			typeError("*** line " + String.valueOf(expr.posn.getPosition()) + ": trying to make a call on a non-method!");
		} else {
		if (((MethodDecl) expr.functionRef.decl).parameterDeclList.size() != expr.argList.size()) {
			typeError("*** line " + String.valueOf(expr.posn.getPosition()) + ": there are " + String.valueOf(expr.argList.size()) + " arguments entered for a method that takes " + String.valueOf(((MethodDecl) expr.functionRef.decl).parameterDeclList.size()) + "!");
		} else {
		for (int i = 0; i < ((MethodDecl) expr.functionRef.decl).parameterDeclList.size(); i++) {
			decl = ((MethodDecl) expr.functionRef.decl).parameterDeclList.get(i).type;
			exprT = (TypeDenoter) visitExpression(expr.argList.get(i), arg);
			if (!exprT.typeKind.equals(TypeKind.NULL)) {
				equals(decl, exprT, expr.posn);			
			}
			
		}
		}
		}
		if (expr.functionRef.decl.type.typeKind.equals(TypeKind.CLASS)) {
			return ((ClassType) expr.functionRef.decl.type).className.decl.type;
		} else {
			return expr.functionRef.decl.type;
		} 
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, String arg) {
		switch (expr.lit.kind) {
		case TRUE:
		case FALSE:
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
		case NUM:
			return new BaseType(TypeKind.INT, expr.posn);
		case NULL:
			return new BaseType(TypeKind.NULL, expr.posn);
		default:
			return new BaseType(TypeKind.UNSUPPORTED, expr.posn);
		}
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, String arg) {
		//TODO: verify new classtype
		// return new ClassType
		
		// prev: return expr.classtype;
		return expr.classtype.className.decl.type; //-- apparently, new strings are fine then
		//return expr.classtype;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, String arg) {
		// TODO verify class
		switch(expr.eltType.typeKind) {
		case INT:
		case BOOLEAN:
		case NULL:
			return new ArrayType(new BaseType(expr.eltType.typeKind, expr.posn), expr.posn);
		case CLASS:
			//return new ArrayType((ClassType) expr.eltType, expr.posn);
			return new ArrayType(((ClassType) expr.eltType).className.decl.type, expr.posn);
		default:
			return new BaseType(TypeKind.UNSUPPORTED, expr.posn);

		}
	}
	
	Object visitReference(Reference r, String arg) throws SyntaxError {
			if (r instanceof ThisRef) {
				return visitThisRef((ThisRef) r, arg);
			} else if (r instanceof IdRef) {
				return visitIdRef((IdRef) r, arg);
			} else if (r instanceof QualRef) {
				return visitQRef((QualRef) r, arg);
			} else {
				typeError("*** line " + String.valueOf(r.posn.getPosition()) + ": Unrecognized type of reference!");
			}
			return null;
	}
	@Override
	public Object visitThisRef(ThisRef ref, String arg) {
		return ((ClassType) ref.decl.type).className.decl.type;
	}

	@Override
	public Object visitIdRef(IdRef ref, String arg) {
		// TODO verify class, used to be no if
		if (ref.decl.type.typeKind.equals(TypeKind.CLASS)) {
			return ((ClassType) ref.decl.type).className.decl.type;
		} else {
			return ref.decl.type;
		}
	}

	@Override
	public Object visitQRef(QualRef ref, String arg) {
		// TODO verify class
		if (ref.decl.type.typeKind.equals(TypeKind.CLASS)) {
			return ((ClassType) ref.decl.type).className.decl.type;
		} else {
			return ref.decl.type;
		}
	}

	@Override
	public Object visitIdentifier(Identifier id, String arg) {
		if (id.decl.type.typeKind.equals(TypeKind.CLASS)) {
			return ((ClassType) id.decl.type).className.decl.type;
		}
		return id.decl.type;
	}

	@Override
	public Object visitOperator(Operator op, String arg) {
		return op;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, String arg) {
		return new BaseType(TypeKind.INT, num.posn);
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, String arg) {
		return new BaseType(TypeKind.BOOLEAN, bool.posn);
	}

	@Override
	public Object visitNullLiteral(NullLiteral nulll, String arg) {
		return new BaseType(TypeKind.NULL, nulll.posn);
	}
	
	


}
