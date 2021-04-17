package miniJava.ContextualAnalysis;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import miniJava.SyntacticAnalyzer.*;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;

public class Identification implements Visitor<String, Object> {

	private IdentificationTable classDecs;
	private IdentificationTable curMemDecs;
	private IdentificationTable otherMemDecs;
	//scoped identification table
	private Stack<IdentificationTable> sit; 
	private MethodDecl curMeth;
	
	private boolean staticMethod;
	
	private AST ast;
	private String currentClass;
	
	ErrorReporter reporter;
	
	public Identification(AST ast, ErrorReporter reporter) {
		this.ast = ast;
		this.reporter = reporter;
		this.sit = new Stack<IdentificationTable>();
		this.curMemDecs = new IdentificationTable();
		this.otherMemDecs = new IdentificationTable();
		this.classDecs = new IdentificationTable();
		this.staticMethod = false;
		addPredefinedClasses();
		visitPackage((Package) ast, "");
	}
	
	private void identificationError (String e) throws SyntaxError{
		reporter.reportError(e);
		//System.out.println(classDecs.toString());
		//System.out.println(curMemDecs.toString());
		//System.out.println(otherMemDecs.toString());
		//System.out.println(sit.peek().toString());
		throw new SyntaxError();
	}
	
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;
	}
	
	Declaration getDecl(String name) {

			Stack<IdentificationTable> holder = new Stack<IdentificationTable>();
			Declaration result = null;
			while(!sit.empty()) {
				if (sit.peek().containsKey(name)) {
					result = sit.peek().get(name);
					break;
				}
				holder.push(sit.pop());
			}
			
			if (result == null) {
				result = curMemDecs.get(name);
			}
			
			
			while(!holder.isEmpty()) {
				sit.push(holder.pop());
			}
			
			return result;
		
	}
	
	private void addPredefinedClasses() {
	//IdentificationTable level1 = new IdentificationTable();
	SourcePosition sp = new SourcePosition(0);
	
	// class System { public static _PrintStream out; }
	FieldDeclList sFdl = new FieldDeclList();
	FieldDecl fd = new FieldDecl(false, true, new ClassType(new Identifier(new Token(TokenKind.ID, "_PrintStream", sp)), sp), "out", sp);
	sFdl.add(fd);
	classDecs.put("System", new ClassDecl("System", sFdl, new MethodDeclList(), sp));
	otherMemDecs.put("System." + fd.name, fd);
	
	// class _PrintStream { public void println(int n){}; }
	MethodDeclList psMdl = new MethodDeclList();
	ParameterDeclList psPdl = new ParameterDeclList();
	psPdl.add(new ParameterDecl(new BaseType(TypeKind.INT, sp), "n", sp));
	MethodDecl md = new MethodDecl(
			new FieldDecl(false, false, new BaseType(TypeKind.VOID, sp), "println", sp), 
			psPdl,
			new StatementList(),
			sp);
	otherMemDecs.put("_PrintStream." + md.name, md);
	psMdl.add(md);
	classDecs.put("_PrintStream", new ClassDecl("_PrintStream", new FieldDeclList(), psMdl, sp));
	
	classDecs.put("String", new ClassDecl("String", new BaseType(TypeKind.UNSUPPORTED, sp), new FieldDeclList(), new MethodDeclList(), sp));
	
	} 

	@Override
	public Object visitPackage(Package prog, String arg) throws SyntaxError {
		// (ClassDeclaration)*eot
		

		try {
		
			for (ClassDecl c : prog.classDeclList) {
				if (classDecs.containsKey(c.name)) {
					identificationError("*** line " + String.valueOf(c.posn.getPosition()) + ": Identification Error: Repeat Class names not allowed, '" + c.name + "' already declared!");
				} else {
					classDecs.put(c.name, c);
				}
				
				for (FieldDecl fd: c.fieldDeclList) {
					if (!fd.isPrivate) {
						if (otherMemDecs.containsKey(c.name + "." + fd.name)) {
							identificationError("*** line " + String.valueOf(fd.posn.getPosition()) + ": Identification Error: Repeat member names not allowed, '" + fd.name + "' already declared in '" + c.name +"'!");
						} else {
							otherMemDecs.put(c.name + "." + fd.name, fd);
						}
					}
				}
				for (MethodDecl md: c.methodDeclList) {
					if (!md.isPrivate) {
						if (otherMemDecs.containsKey(c.name + "." + md.name)) {
							identificationError("*** line " + String.valueOf(md.posn.getPosition()) + ": Identification Error: Repeat member names not allowed, '" + md.name + "' already declared in '" + c.name +"'!");
						} else {
							otherMemDecs.put(c.name + "." + md.name, md);
						}
					}
				}
			}
			
			for (ClassDecl c: prog.classDeclList) {
				curMemDecs = new IdentificationTable();
				currentClass = c.name;
				visitClassDecl(c, c.name); // all args have current class name
			}
			
			return null;
		} catch (SyntaxError e) {
			return null;
		}
		
	}
	
	
	@Override
	public Object visitClassDecl(ClassDecl cd, String arg) throws SyntaxError {
		// class id { ( FieldDeclaration | MethodDeclaration )* }
		

		try {
			for (FieldDecl fd: cd.fieldDeclList) {
				visitFieldDecl(fd, arg);
			}
			
			for (MethodDecl md: cd.methodDeclList) {
				if (curMemDecs.containsKey(md.name)) {
					identificationError("*** line " + String.valueOf(md.posn.getPosition()) + ": Identification Error: Repeat member names not allowed, '" + md.name + "' already declared in '" + cd.name +"'!");
				} else {
					curMemDecs.put(md.name, md);
				}
			}
			
			for (MethodDecl md: cd.methodDeclList) {
				visitMethodDecl(md, arg);
			}
			

			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, String arg) throws SyntaxError {
		// Visibility Access Type id ;
		try {
			if (curMemDecs.containsKey(fd.name)) {
				identificationError("*** line " + String.valueOf(fd.posn.getPosition()) + ": Identification Error: Repeat member names not allowed, '" + fd.name + "' already declared in '" + currentClass +"'!");
			} else {
				curMemDecs.put(fd.name, fd);
			}
			if (fd.type.typeKind.equals(TypeKind.CLASS)) {
				visitClassType((ClassType) fd.type, arg);
			} else if (fd.type.typeKind.equals(TypeKind.ARRAY)) {
				visitArrayType((ArrayType) fd.type, arg);
			}
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, String arg) throws SyntaxError {
		// Visibility Access ( Type | void ) id ( ParameterList? ) {Statement*}
		try {
			curMeth = md;
			
			if (md.isStatic) {
				staticMethod = true;
			}
			IdentificationTable paramScope = new IdentificationTable();
				
			sit.add(paramScope);
				
			// Type id ( , Type id )*
			for (ParameterDecl p: md.parameterDeclList) {
				visitParameterDecl(p, arg);
			}
				
			IdentificationTable lev4 = new IdentificationTable();
			sit.add(lev4);
			
			if (!curMeth.type.typeKind.equals(TypeKind.VOID) && md.statementList.size() < 1) {
				identificationError("*** line " + String.valueOf(md.posn.getPosition()) + ": Error! Non-void method must have return statement!");
			}
			
			for (int i = 0; i < md.statementList.size(); i++) {
				if (!curMeth.type.typeKind.equals(TypeKind.VOID)) {
					if (i == md.statementList.size() - 1) {
						// last statement in non-void method has to be return
						visitStatement(md.statementList.get(i), arg + " !last");
					} else {
						visitStatement(md.statementList.get(i), arg);
					}
				} else {
					visitStatement(md.statementList.get(i), arg);
				}
			}

				
			sit.pop(); //removes lev4 scope
			sit.pop(); //removes paramScope
			staticMethod = false;
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}
	
	@Override
	public Object visitParameterDecl(ParameterDecl pd, String arg) throws SyntaxError {
		// Type id 
		try {
			if (sit.peek().containsKey(pd.name)) {
				identificationError("*** line " + String.valueOf(pd.posn.getPosition()) + ": Identification Error: Repeat parameter names not allowed, '" + pd.name + "' already declared in '" + pd.name + "'!");
			} else {
				sit.peek().put(pd.name, pd);
				if (pd.type.typeKind.equals(TypeKind.CLASS)) {
					visitClassType((ClassType) pd.type, arg);
				} else if (pd.type.typeKind.equals(TypeKind.ARRAY)) {
					visitArrayType((ArrayType) pd.type, arg);

				}
			}
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitVarDecl(VarDecl decl, String arg) throws SyntaxError {
		try {
			
			if (decl.type.typeKind.equals(TypeKind.CLASS)) {
				visitClassType((ClassType) decl.type, arg);
			} else if (decl.type.typeKind.equals(TypeKind.ARRAY)) {
				visitArrayType((ArrayType) decl.type, arg);
			}
			
			
			Stack<IdentificationTable> holder = new Stack<IdentificationTable>();
			while(!sit.empty()) {
				if (sit.peek().containsKey(decl.name)) {
					identificationError("*** line " + String.valueOf(decl.posn.getPosition())+ ": Identification Error: Local variables cannot hide parameter and higher levels of scope within method, '" + decl.name + "' already declared before!");
				}
				holder.push(sit.pop());
			}
			
			while(!holder.isEmpty()) {
				sit.push(holder.pop());
			}
			
			sit.peek().put(decl.name, decl);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitBaseType(BaseType type, String arg) {
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, String arg) {		
		try {
			type.className.decl = (ClassDecl) classDecs.get(type.className.spelling);
			if (type.className.decl == null) {
				identificationError("*** line " + String.valueOf(type.posn.getPosition()) + ": Identification Error: Class type '" + type.className.spelling + "' not declared!!");
			}
			return type.className.decl;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitArrayType(ArrayType type, String arg) {
		try {
			if (type.eltType.typeKind.equals(TypeKind.CLASS)) {
				Declaration d = ((Declaration) visitClassType((ClassType) type.eltType, arg));
				((ClassType) type.eltType).className.decl = d;
				type.eltType = d.type;
			}
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}
	
	public Object visitStatement(Statement s, String arg) throws SyntaxError{
		// redirect
		try {
			String[] args = arg.split("\\s+");
			
			if (args.length > 1) {
				arg = args[0];
			}
			
			if (args.length > 1 && args[1].equals("!last") && !(s instanceof ReturnStmt)) {
				identificationError("*** line " + String.valueOf(s.posn.getPosition()) + ": Error! Last statement in non-void method must be return statement!");
			}
			
			if (s instanceof BlockStmt) {
				visitBlockStmt((BlockStmt) s, arg);
			} else if (s instanceof VarDeclStmt) {
				if (args.length > 1 && args[1].equals("!conditionbranch")) {
					identificationError("*** line " + String.valueOf(s.posn.getPosition()) + ": Identification Error: statement in a condition branch cannot be only variable declaration!");
				}
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
				identificationError("*** line " + String.valueOf(s.posn.getPosition()) + ": Statement not recognized as particular type!");
			}
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, String arg) throws SyntaxError{
		//  { Statement* }
		try {
			IdentificationTable newLevel = new IdentificationTable();
			sit.add(newLevel);
			
			for (Statement s: stmt.sl) {
				visitStatement(s, arg);
			}
			
			sit.pop(); //removes newLevel scope
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, String arg) throws SyntaxError {
		// Type id = Expression ;
		try {
			visitExpression(stmt.initExp, arg + " " + stmt.varDecl.name);
			visitVarDecl(stmt.varDecl, arg);
		
		
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, String arg) throws SyntaxError {
		// Reference = Expression ;
		try {
			visitReference(stmt.ref, arg);
			visitExpression(stmt.val, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, String arg) throws SyntaxError {
		// Reference [ Expression ] = Expression ;
		try {
			visitReference(stmt.ref, arg);
			visitExpression(stmt.ix, arg);
			visitExpression(stmt.exp, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, String arg) throws SyntaxError {
		// Reference ( ArgumentList? ) ;
		// ArgumentList ::= Expression ( , Expression )*
		try {
			stmt.methodRef.decl = (Declaration) visitReference(stmt.methodRef, arg + " !callExpr");
			if (stmt.argList.size() > 0) {
				for (Expression e: stmt.argList) {
					visitExpression(e, arg);
				}
			}
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, String arg) throws SyntaxError {
		// return Expression? ;
		try {
			if (stmt.returnExpr != null) {
				visitExpression(stmt.returnExpr, arg);
			}
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, String arg) throws SyntaxError{
		// if ( Expression ) Statement (else Statement)?
		try {
			visitExpression(stmt.cond, arg);
			
			visitStatement(stmt.thenStmt, arg + " !conditionbranch");
			if (stmt.elseStmt != null) {
				visitStatement(stmt.elseStmt, arg + " !conditionbranch");
			}
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, String arg) throws SyntaxError {
		// while ( Expression ) Statement
		try {
			visitExpression(stmt.cond, arg);
			visitStatement(stmt.body, arg + " !conditionbranch");	
	
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}
	
	Object visitExpression(Expression e, String arg) throws SyntaxError{
		try {
			if (e instanceof UnaryExpr) {
				visitUnaryExpr((UnaryExpr) e, arg);
			} else if (e instanceof BinaryExpr) {
				visitBinaryExpr((BinaryExpr) e, arg);
			} else if (e instanceof RefExpr) {
				visitRefExpr((RefExpr) e, arg);
			} else if (e instanceof IxExpr) {
				visitIxExpr((IxExpr) e, arg);
			} else if (e instanceof CallExpr) {
				visitCallExpr((CallExpr) e, arg);
			} else if (e instanceof LiteralExpr) {
				visitLiteralExpr((LiteralExpr) e, arg);
			} else if (e instanceof NewObjectExpr) {
				visitNewObjectExpr((NewObjectExpr) e, arg);
			} else if (e instanceof NewArrayExpr) {
				visitNewArrayExpr((NewArrayExpr) e, arg);
			} else if (e instanceof ArrayLengthExpr) {
				visitArrayLengthExpr((ArrayLengthExpr) e, arg);
			} else {
				identificationError("*** line " + String.valueOf(e.posn.getPosition()) + ": Expression not recognized type!");
			}
			
			return null;
		} catch (SyntaxError er) {
			throw er;
		}
	} 

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, String arg) throws SyntaxError {
		// unop Expression
		try {
			visitExpression(expr.expr, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, String arg) throws SyntaxError {
		// Expression binop Expression
		try {
			visitExpression(expr.left, arg);
			visitExpression(expr.right, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitRefExpr(RefExpr expr, String arg) throws SyntaxError {
		// Reference
		String[] args = arg.split("\\s+");
		try {

			visitReference(expr.ref, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitIxExpr(IxExpr expr, String arg) throws SyntaxError {
		// Reference [ Expression ]
		try {
			visitReference(expr.ref, arg);
			visitExpression(expr.ixExpr, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitCallExpr(CallExpr expr, String arg) throws SyntaxError{
		// Reference ( ArgumentList? )
		try {
			visitReference(expr.functionRef, arg + " !callExpr");
			if (expr.argList.size() > 0) {
				for (Expression e: expr.argList) {
					visitExpression(e, arg);
				}
			}
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, String arg) throws SyntaxError {
		// num | true | false | null
		try {
			switch (expr.lit.kind) {
			case NULL:
				visitNullLiteral((NullLiteral) expr.lit, arg);
				break;
			case BOOLEAN:
			case TRUE:
			case FALSE:
				visitBooleanLiteral((BooleanLiteral) expr.lit, arg);
				break;
			case NUM:
				visitIntLiteral((IntLiteral) expr.lit, arg);
				break;
			default:
				identificationError("*** line " + String.valueOf(expr.posn.getPosition()) + ": Literal expression type not recognized!");
			}
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, String arg) throws SyntaxError{
 
		try {
			visitClassType(expr.classtype, arg);
	
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, String arg) throws SyntaxError{
		// new ( int [ Expression ] | id [ Expression ] )
		try {
			switch (expr.eltType.typeKind) {
			case INT:
				break;
			case CLASS:
				visitClassType((ClassType) expr.eltType, arg);
				break;
			default:
				identificationError("*** line " + String.valueOf(expr.posn.getPosition()) + ": Error! new array object can only be int or class type!!");
			}
			
			visitExpression(expr.sizeExpr, arg);
			
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}
	
	// added for pa4
	
	Object visitReference(Reference r, String arg) throws SyntaxError {
		try {
			if (r instanceof ThisRef) {
				return visitThisRef((ThisRef) r, arg);
			} else if (r instanceof IdRef) {
				return visitIdRef((IdRef) r, arg);
			} else if (r instanceof QualRef) {
				return visitQRef((QualRef) r, arg);
			} else {
				identificationError("*** line " + String.valueOf(r.posn.getPosition()) + ": Unrecognized type of reference!");
			}
			return null;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitThisRef(ThisRef ref, String arg) {
		try {
			String[] args = arg.split("\\s+");

			if (staticMethod) {
				identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: cannot reference 'this' in static context!");
			}
			
			ref.decl = classDecs.get(currentClass);
			if (ref.decl.type.typeKind.equals(TypeKind.CLASS)) {
				visitClassType((ClassType) ref.decl.type, arg);
			}
			return ref.decl;
			
		} catch (SyntaxError e) {
			throw e;
		}
	}

	
	@Override
	public Object visitIdRef(IdRef ref, String arg) {
		// id
		try {
			
			
			ref.decl = getDecl(ref.id.spelling);
			
			if (ref.decl != null) {
				ref.decl = (Declaration) visitIdentifier(ref.id, arg);
			}
			
			if (ref.decl == null) {
				// case member of current class
				ref.decl = curMemDecs.get(ref.id.spelling);
			}
			if (ref.decl == null) {
				// case static class reference
				ref.decl = classDecs.get(ref.id.spelling);
				if(!in(arg, "!qualified")) {
					identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: static class is not variable!");
				}
			} 
			
			
			if (ref.decl instanceof MethodDecl) {

				if (! in(arg, "!callExpr")) {
					identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: Method needs to be used in call expression!");
				}
			}
			
			if (in(arg, ref.id.spelling) && !in(arg, "!qualified")) {
				identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: cannot use variable name in declaration!");
			}

			
			if (ref.decl == null) {
				identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: Member '" + ref.id.spelling + "' does not exist or is not accessible in specified class!");
			}
			
			
			if (ref.decl.type.typeKind.equals(TypeKind.CLASS)) {
				visitClassType((ClassType) ref.decl.type, arg);
			}
			if (ref.decl.type.typeKind.equals(TypeKind.ARRAY)) {
				visitArrayType((ArrayType) ref.decl.type, arg);
			}
			
			return ref.decl;
					
		} catch (SyntaxError e) {
			throw e;
		}
	}
	
	public boolean in(String args, String arg) {
		boolean found = false;
		String[] argss = args.split("\\s+");
		for (String i : argss) {
			if (i.equals(arg)) {
				found = true;
				break;
			}
		}
		return found;
	}

	@Override
	public Object visitQRef(QualRef ref, String arg) {
		// Reference.id
		
		String[] args = arg.split("\\s+");
		
		try {
			Declaration prevDecl = null;
			
			if (in(arg, "!qualified")) {
				prevDecl = (Declaration) visitReference(ref.ref, arg);

			} else {
				prevDecl = (Declaration) visitReference(ref.ref, arg + " !qualified");
			}
			
			
			if (prevDecl instanceof ClassDecl) {

				if (otherMemDecs.containsKey(prevDecl.name + "." + ref.id.spelling)) {
					ref.decl = (MemberDecl) otherMemDecs.get(prevDecl.name + "." + ref.id.spelling);
				} else {
					boolean cc = false;
					for (String i : args) {
						if (i.equals(currentClass)) {
							cc = true;
						}
					}
					if (!cc || !curMemDecs.containsKey(ref.id.spelling)) {
						identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: member '" + ref.id.spelling + "' not found for class type '" + currentClass + "' or not accessible from current scope!");
					} else {
						ref.decl = (MemberDecl) curMemDecs.get(ref.id.spelling);
					}
				}
				
				if (staticMethod && !((MemberDecl) ref.decl).isStatic) {
					identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ":  cannot access non-static member '" + ref.decl.name + "' in a static method!");
				}

			} else if (prevDecl instanceof MemberDecl) {
				if (! ((MemberDecl) prevDecl).type.typeKind.equals(TypeKind.CLASS)){
					identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: Variable '" + prevDecl.name + "' is not class type!");
					return prevDecl;
				} 
				ClassDecl cd = (ClassDecl) visitClassType((ClassType)((MemberDecl) prevDecl).type, arg);
				
				if (otherMemDecs.containsKey(cd.name + "." + ref.id.spelling)) {
					ref.decl = (MemberDecl) otherMemDecs.get(cd.name + "." + ref.id.spelling);
				} else {
					boolean cc = false;
					for (String i : args) {
						if (i.equals(currentClass)) {
							cc = true;
						}
					}
					if (!cc || !curMemDecs.containsKey(ref.id.spelling)) {
						identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: member '" + ref.id.spelling + "' not found for class type '" + cd.name + "' or not accessible from current scope!");
					} else {
						ref.decl = (MemberDecl) curMemDecs.get(ref.id.spelling);
					}
				}
			} else if (prevDecl instanceof MethodDecl) {
				identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ":  method '" + prevDecl.name + "' cannot be used as qualified reference!");
			} else {
				// scoped reference
				if (!prevDecl.type.typeKind.equals(TypeKind.CLASS)) {
					identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Cannot have qualified reference for variable of type " + prevDecl.type.typeKind + "!");
				}
				ClassDecl cd = (ClassDecl) visitClassType((ClassType)(prevDecl).type, arg);
				
				if (otherMemDecs.containsKey(cd.name + "." + ref.id.spelling)) {
					ref.decl = (MemberDecl) otherMemDecs.get(cd.name + "." + ref.id.spelling);
				} else {
					if (!in(arg, currentClass) || !curMemDecs.containsKey(ref.id.spelling)) {
						identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: member '" + ref.id.spelling + "' not found for class type '" + cd.name + "' or not accessible from current scope!");
					} else {
						ref.decl = (MemberDecl) curMemDecs.get(ref.id.spelling);
					}
				}
			}
			
			if (ref.decl instanceof MethodDecl && !in(arg, "!callExpr")){
				identificationError("*** line " + String.valueOf(ref.posn.getPosition()) + ": Identification Error: '" +  ref.decl.name + "' is not used properly in a call expression!");
			}
			
			if (ref.decl.type.typeKind.equals(TypeKind.CLASS)) {
				visitClassType((ClassType) ref.decl.type, arg);
			}
			if (ref.decl.type.typeKind.equals(TypeKind.ARRAY)) {
				visitArrayType((ArrayType) ref.decl.type, arg);
			}
			
			return ref.decl;
			
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitIdentifier(Identifier id, String arg) {
		try {
			id.decl = getDecl(id.spelling);
			if (id.decl == null) {
				identificationError("*** line " + String.valueOf(id.posn.getPosition()) + ": Identification Error: '" + id.spelling + "' has not been declared yet!");
			}
			
			if (id.decl.type.typeKind.equals(TypeKind.CLASS)) {
				visitClassType((ClassType) id.decl.type, arg);
			}
			if (id.decl.type.typeKind.equals(TypeKind.ARRAY)) {
				visitArrayType((ArrayType) id.decl.type, arg);
			}
			
			if (id.decl instanceof MemberDecl && staticMethod && !((MemberDecl) id.decl).isStatic) {
				// cannot directly access a non-static member of class c if in a static method in class c
				identificationError("*** line " + String.valueOf(id.posn.getPosition()) + ":  cannot access non-static member '" + id.decl.name + "' in a static method!");
				
			}
			return id.decl;
		} catch (SyntaxError e) {
			throw e;
		}
	}

	@Override
	public Object visitOperator(Operator op, String arg) {
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, String arg) {
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, String arg) {
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nulll, String arg) {
		return null;
	}

	@Override
	public Object visitArrayLengthExpr(ArrayLengthExpr al, String arg) {
		visitReference(al.r, arg);
		return null;
	}

}