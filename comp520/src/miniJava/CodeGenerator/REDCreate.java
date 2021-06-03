package miniJava.CodeGenerator;

import java.util.ArrayList;

import mJAM.*;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.Machine.Reg;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.SyntacticAnalyzer.TokenKind;

public class REDCreate implements Visitor<String, Object> {
	
	/*
	 * PA5 extensions:
	 * - 2 static field initialization (i think i already have this)
	 * - 2 parameterized class constructors (There should only be one constructor per 
	 * class, but it may have parameters. If none is defined, the default constructor should be available.
	 * - 3 for loops. Be sure to consider the possible forms of the initialization (including declaration of the iterator variable), loop test, and increment parts
	 * - 1 - 3: Improve code generation for the condition (test) in while and if statements,
focusing on efficient evaluation of short-circuit boolean operators && and ||.
Ideally, efficient evaluation means: (1) minimize alternation between jumps and
construction of truth values on the stack and (2) no chains of consecutive jumps
without intervening tests in the evaluation of a conditional expression. Partial
credit is available for anything that improves on the base strategy.

- 4 Add the String type and string literals. No operations need to be supported on
strings, but you must be able to assign a string literal or a String reference to a
variable of type String, and it must be possible to print String values by
overloading System.out.println().

- 5 Add overloaded methods that differ in the types of their arguments, and
perform type checking to determine their validity and to resolve overloading.
	 */
	
	AST tree;
	ErrorReporter reporter;
	int staticOffset;
	int offset;
	int main;
	int blockOffset;
	int returnOffset;
	int numBlocks;
	MethodDecl mainMeth;
	ArrayList<Patch> patches;
	MethodDecl curMeth;

	/*
	 * encodeFetch and encodeAssign
	 */
	
	// call for static methods, calli for instance methods, calld for dynamic method invocation

	public REDCreate(AST tree, ErrorReporter reporter) {
		this.tree = tree;
		this.reporter = reporter;
		

		curMeth = null;
		offset = 0;
		staticOffset = 0;
		returnOffset = 0;
		mainMeth = null;
		patches = new ArrayList<Patch>();
		blockOffset = 0;
		numBlocks = 0;
		
		visitPackage((Package) tree, "");
		
		
	}
	
	private void REDError (String e) throws REDError{
		reporter.reportError(e);
		//throw new REDError();
	}
	
	class REDError extends Error {
		private static final long serialVersionUID = 1L;
	}
	
	boolean in(String args, String arg) {
		String[] argss = args.split("\\s+");
		for (String ar : argss) {
			if (ar.equals(arg)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Declarations: frame description passed in, yields the amount of storage allocated by declaration
	 * Commands: frame description passes in, yields null
	 * Expressions: frame description passes in, yields the size of the result
	 * V-names (references) gets the frame description passed in, yields the runtime entity description of the reference
	 */

	@Override
	public Object visitPackage(Package prog, String arg) {
		
		// first pass to go through and generate runtime entities for each (?) of the declarations
		for (ClassDecl c : prog.classDeclList) {

			visitClassDecl(c, arg);
					
			for (MethodDecl md : c.methodDeclList) {
				
				if (!md.isPrivate && md.isStatic 
						&& md.type.typeKind.equals(TypeKind.VOID) 
						&& md.name.equals("main")) {
					if (md.parameterDeclList.size() > 1 || md.parameterDeclList.size() == 0) {
						//REDError("Error! Public static void main method needs one input argument!");
						continue;
					} else {
						if (md.parameterDeclList.size() > 1) {
							continue;
						}
						ParameterDecl pd = md.parameterDeclList.get(0);
						if (!pd.type.typeKind.equals(TypeKind.ARRAY)) {
							//REDError("Error! Public static void main method needs String[] input argument type!");
							continue;
						} else {
							if (!((ArrayType) pd.type).eltType.typeKind.equals(TypeKind.STRING)) {
								//REDError("Error! Public static void main method needs String[] input argument type!");
								continue;
							}
							
							if ((mainMeth != null)) {
								REDError("*** Error: Cannot have mutiple public static void main methods!");
							} else {
								mainMeth = md;
							}
						}
						
					}
				}
			}
		}
		
		if (mainMeth == null) {
			REDError("*** Error: Need a unique public static void main method for the program!");
			return null;
		} else {
			// before main method call, need to load all our static variable initializations
			for (ClassDecl c : prog.classDeclList) {
				for (FieldDecl fd : c.fieldDeclList) {
					visitFieldDecl(fd, arg);
				}
			}
			
			
			Machine.emit(Op.LOADL, 0);
			Machine.emit(Prim.newarr);
			
			patches.add(new Patch(mainMeth, Machine.nextInstrAddr()));
			Machine.emit(Op.CALL, Reg.CB, 0);
			Machine.emit(Op.HALT, 0, 0, 0); // run [[c]] = execute [[C]] HALT
			
			
			for (ClassDecl c : prog.classDeclList) {
				visitClassDecl(c, arg); 
			}

			
			for (Patch p : patches) {
				Machine.patch(p.address, p.md.re.offset);
			}
			
		}
		
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, String arg) {
		// what if the class has no fields or methods?
		if (cd.re == null) {
			// first pass
	
			int displacement = 0;
			int staticFields = 0;
			for (FieldDecl fd : cd.fieldDeclList) {
				visitFieldDecl(fd, String.valueOf(displacement));
				if (!fd.isStatic) {
					displacement++;
				} else {
					staticFields++;
				}
			}
			if (cd.fieldDeclList.size() - staticFields < 1) {
				cd.re = new RuntimeEntity(1, Reg.OB, 0); // second one needs changed

			} else {
				cd.re = new RuntimeEntity(cd.fieldDeclList.size() - staticFields, Reg.OB, 0); // second one needs changed

			}

			
			for (MethodDecl md : cd.methodDeclList) {
				visitMethodDecl(md, arg);
			}
			return cd.fieldDeclList.size() - staticFields;
		} else {
			// second pass code generation
			
			/*for (FieldDecl fd : cd.fieldDeclList) {
				visitFieldDecl(fd, arg);
			}*/
			
			for (MethodDecl md : cd.methodDeclList) {
				curMeth = md;
				visitMethodDecl(md, arg);
			}
			return null;
		}
		
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, String arg) {
		// what if the field is private static? I guess it can't be accessed through the thing
		if (fd.re == null) {
			// first pass
			if (fd.isStatic) {
				Machine.emit(Op.PUSH, 1);
				fd.re = new RuntimeEntity(1, Reg.SB, staticOffset);
				staticOffset++;
			} else {
				fd.re = new RuntimeEntity(1, Reg.OB, Integer.valueOf(arg));
			}
			return 1;
		} else {
			// second pass static values??? have to initialize them on second pass 
			// once the class runtime entities are determined
			if (fd.isStatic && fd.ix != null) {
				visitExpression(fd.ix, arg);
				Machine.emit(Op.STORE, fd.re.reg, fd.re.offset); 
			}
			return null;
		}
		
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, String arg) {
		// what if method doesn't have any statements? what if it has only a return?
		// what if it has a return and nothing else;
		if (md.re == null) {
			int i = -1;
			for (ParameterDecl pd : md.parameterDeclList) {
				visitParameterDecl(pd, String.valueOf(i));
				i--;
			}
			md.re = new RuntimeEntity(1, Reg.CB, 0); // ??? idk slide 8 of miniJavaCodeGenExample
		} else {
			
			numBlocks = 0;
			offset = 3;
			// second arg is offset from codebase
			md.re = new RuntimeEntity(1, Reg.CB, Machine.nextInstrAddr()); 

			for (ParameterDecl pd : md.parameterDeclList) {
				//visitParameterDecl(pd, arg);
			}
			
			for (Statement s: md.statementList) {
				visitStatement(s, arg);
			}
			
			if (md.type.typeKind.equals(TypeKind.VOID)) {
				Machine.emit(Op.RETURN, 0, 0, md.parameterDeclList.size()); 
			} else {
				// in return statement which is required
			}
		}
		
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, String arg) {
		pd.re = new RuntimeEntity(1, Reg.LB, Integer.valueOf(arg)); // ???
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, String arg) {
		// equals null
		
		// put a new object on the stack
		
		Machine.emit(Op.PUSH, 1);
		
		decl.re = new RuntimeEntity(1, Reg.LB, offset);
		if (in(arg, "!block")) {
			blockOffset++;
		} 
		offset++;
		return 1;
	}

	@Override
	public Object visitBaseType(BaseType type, String arg) {
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, String arg) {
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, String arg) {
		return null;
	}
	
	public Object visitStatement(Statement s, String arg){
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
			} 
			return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, String arg) {
		int oldOffset = blockOffset; // for nested blocks
		blockOffset = 0;
		returnOffset = 0;
		for (Statement s : stmt.sl) {
			visitStatement(s, arg + " !block");
		}
		Machine.emit(Op.POP, blockOffset + returnOffset);
		offset -= blockOffset;
		blockOffset = oldOffset;
		
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, String arg) {

		
		visitVarDecl(stmt.varDecl, arg);
		visitExpression(stmt.initExp, arg);
		
		Machine.emit(Op.STORE, stmt.varDecl.re.reg, stmt.varDecl.re.offset); 

		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, String arg) {
		
		
		if (stmt.ref.decl instanceof FieldDecl && ((FieldDecl) stmt.ref.decl).isStatic) {
			// static
			visitExpression(stmt.val, arg);
			encodeStore(stmt.ref);
		} else if (stmt.ref instanceof QualRef) {
			// qualified
			encodeStore(stmt.ref);
			visitExpression(stmt.val, arg);
			Machine.emit(Prim.fieldupd);
		} else {
			// local
			visitExpression(stmt.val, arg);
			encodeStore(stmt.ref);
		}
		return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, String arg) {
		visitReference(stmt.ref, arg); // puts address   of the array on the stack
		visitExpression(stmt.ix, arg); // puts expression i on stack
		visitExpression(stmt.exp, arg); // puts new value on stack
		Machine.emit(Prim.arrayupd); // takes all three args and updates
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, String arg) {
		for (int i = stmt.argList.size() - 1; i >= 0; i--) {
			visitExpression(stmt.argList.get(i), arg); //load last ones first
		}
		visitReference(stmt.methodRef, " !method"); // puts address of the reference on the stack
		if (!((MethodDecl) stmt.methodRef.decl).type.typeKind.equals(TypeKind.VOID)){
			// not void
			/*
			if (in(arg, "!block")) {
				blockOffset++;
			}*/
			if (in(arg, "!block")) {
				returnOffset++;
			}
		}
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, String arg) {
		if (stmt.returnExpr != null) {
			visitExpression(stmt.returnExpr, arg); // puts return value on stack top
			Machine.emit(Op.RETURN, 1, 0, curMeth.parameterDeclList.size()); 
		} else {
			Machine.emit(Op.RETURN, 0, 0, curMeth.parameterDeclList.size()); 
		}
		
		
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, String arg) {
		
		visitExpression(stmt.cond, arg);
		// use patch to figure out lines of code
		int jumpIfAddr = Machine.nextInstrAddr();
		Machine.emit(Op.JUMPIF, 0, Reg.CB, 0); // if false go to
		visitStatement(stmt.thenStmt, arg);
		
		int jumpAddr = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, 0);
		// false statement
		
		int falseAddr = Machine.nextInstrAddr(); // relative to CB
		visitStatement(stmt.elseStmt, arg);
		int afterElseAddr = Machine.nextInstrAddr();
		
		Machine.patch(jumpIfAddr, falseAddr);
		Machine.patch(jumpAddr, afterElseAddr);
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, String arg) {
		
		int jumpAddr = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, 0); //needs patched to eval statement
		
		int jumpDo = Machine.nextInstrAddr();
		visitStatement(stmt.body, arg);
		
		int jumpEval = Machine.nextInstrAddr();
		visitExpression(stmt.cond, arg);
		
		Machine.emit(Op.JUMPIF, 1, Reg.CB, jumpDo); // if true run the code
		
		Machine.patch(jumpAddr, jumpEval);
		
		
		return null;
	}
	
	Object visitExpression(Expression e, String arg) {

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
			} 
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, String arg) {
		visitExpression(expr.expr, arg);
		visitOperator(expr.operator, arg + " !unExpr");
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, String arg) {
		int patchAddress;
		int jumpTo;
		if (expr.operator.spelling.equals("||")) {
			visitExpression(expr.left, arg);
			patchAddress = Machine.nextInstrAddr();
			Machine.emit(Op.JUMPIF, 1, Reg.CB, 0); // if true skip over all this business, eats the actual value
			
			Machine.emit(Op.LOADL, 0); // reload the eaten value
			visitExpression(expr.right, arg);
			visitOperator(expr.operator, arg + " !binExpr");
			Machine.emit(Op.JUMP, Reg.CB, Machine.nextInstrAddr() + 2); // you need to skip over where the 1 is reloaded
			// (the jumpif command eats the value)
			
			jumpTo = Machine.nextInstrAddr();
			Machine.emit(Op.LOADL, 1);
			Machine.patch(patchAddress, jumpTo);
			
		} else if (expr.operator.spelling.equals("&&")) {
			visitExpression(expr.left, arg);
			patchAddress = Machine.nextInstrAddr();
			Machine.emit(Op.JUMPIF, 0, Reg.CB, 0); // if true run the code, eats the actual value
			
			Machine.emit(Op.LOADL, 1); // reload the eaten value
			visitExpression(expr.right, arg);
			visitOperator(expr.operator, arg + " !binExpr");
			Machine.emit(Op.JUMP, Reg.CB, Machine.nextInstrAddr() + 2);
			
			jumpTo = Machine.nextInstrAddr();
			Machine.emit(Op.LOADL, 0);
			Machine.patch(patchAddress, jumpTo);
			
		} else {
			visitExpression(expr.left, arg);
			visitExpression(expr.right, arg);
			
			visitOperator(expr.operator, arg + " !binExpr");
		}
		
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, String arg) {
		visitReference(expr.ref, "");
		return null;
	}

	@Override
	public Object visitIxExpr(IxExpr expr, String arg) {
		visitReference(expr.ref, arg); // puts array address on stack
		visitExpression(expr.ixExpr, arg); // puts expression i on stack
		Machine.emit(Prim.arrayref); // puts the value from the array on the stack
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, String arg) {
		for (int i = expr.argList.size() - 1; i >= 0; i--) {
			visitExpression(expr.argList.get(i), arg); //load last ones first
		}
		
		visitReference(expr.functionRef, " !method"); // puts address of the reference on the stack
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, String arg) {
		switch(expr.lit.kind) {
		case NUM:
			visitIntLiteral((IntLiteral) expr.lit, arg);
			break;
		case TRUE:
		case FALSE:
			visitBooleanLiteral((BooleanLiteral) expr.lit, arg);
			break;
		case NULL:
			visitNullLiteral((NullLiteral) expr.lit, arg);
			break;
		case STRINGLIT:
			visitStringLiteral((StringLiteral) expr.lit, arg);
			break;
		default: 

		}
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, String arg) {
		Machine.emit(Op.LOADL, -1); // need to change if i implement inheritance
		Machine.emit(Op.LOADL, expr.classtype.className.decl.re.size);
		Machine.emit(Prim.newobj);
		
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, String arg) {
		visitExpression(expr.sizeExpr, arg); // puts # of elements on stack
		Machine.emit(Prim.newarr);
		return null;
	}
	
	Object visitReference(Reference r, String arg){
		
		if (r instanceof ThisRef) {
			return visitThisRef((ThisRef) r, arg);
		} else if (r instanceof IdRef) {
			return visitIdRef((IdRef) r, arg);
		} else if (r instanceof QualRef) {
			return visitQRef((QualRef) r, arg);
		} 
		
		return null;
	}
	
	
	public void encodeStore(Reference r) {
		// R denotes a localdecl or fielddecl, pop value from stack top and store it in R
		if (r.decl instanceof FieldDecl && ((FieldDecl) r.decl).isStatic) {
			Machine.emit(Op.STORE, r.decl.re.reg, r.decl.re.offset);
		} else if (r instanceof IdRef) {
			Machine.emit(Op.STORE, r.decl.re.reg, r.decl.re.offset);
		} else if (r instanceof QualRef) {
			visitQRef((QualRef) r, " !encode");
			//Machine.emit(Op.STORE, Reg.OB, ((UnknownValue) r.decl.re).address);
		}
		
		// what if reference is this ref? --> illegal, can't be assigned
		// what if length field? --> illegal, can't be assigned
		
	}

	@Override
	public Object visitThisRef(ThisRef ref, String arg) {
		// loads object base of current LB onto the stack --> Reg.OB holds this value
		//System.out.println();
		Machine.emit(Op.LOADA, ref.decl.re.reg, ref.decl.re.offset); 
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, String arg) {
		if (ref.decl instanceof MethodDecl) {
			
			if (((MethodDecl) ref.decl).isStatic) {
				patches.add(new Patch((MethodDecl) ref.decl, Machine.nextInstrAddr()));
				
				Machine.emit(Op.CALL, ref.decl.re.reg, ref.decl.re.offset); // offset from code base 
			} else {
				// implicit this.method
				Machine.emit(Op.LOADA, Reg.OB, 0); 
				patches.add(new Patch((MethodDecl) ref.decl, Machine.nextInstrAddr()));
				Machine.emit(Op.CALLI, ref.decl.re.reg, ref.decl.re.offset); // offset from code base 
			}
			
			
		} else {
			if (ref.decl instanceof ClassDecl && ref.id.spelling.equals(ref.decl.name)) {
				
				// fake System.out.println should be VarDecl (?)
				if (in(ref.id.spelling + "." + arg, "System.out.println")){
					return 1;
				} else {
					Machine.emit(Op.LOADL, 0); // placeholder value
				}
				
			} else {
				Machine.emit(Op.LOAD, ref.decl.re.reg, ref.decl.re.offset);
			}
		} 
		
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, String arg) {
		boolean encode = false;

		if (in(arg, "!encode")) {
			encode = true;
		}
		String[] argss = arg.split("\\s+");

		Object print = null;
		

		if (argss[0].equals("")) { // no more qualifiers afterward
			print = visitReference(ref.ref, ref.id.spelling + arg);
			if (print != null) {
				//System.out.println(ref.decl.name);
				if (ref.decl instanceof MethodDecl && ((MethodDecl) ref.decl).parameterDeclList.get(0).type.typeKind.equals(TypeKind.STRING)) {
					// address of the string literal should be loaded

					Machine.emit(Op.LOAD, Reg.ST, -1); // address loaded again
					Machine.emit(Prim.arraylen); // get the arraylen
					Machine.emit(Op.LOADL, 0); // int i
					int jumpAddr = Machine.nextInstrAddr();
					Machine.emit(Op.JUMP, Reg.CB, 0); // need CB to patch to
					// visitStatement
					int jumpDo = Machine.nextInstrAddr();
					Machine.emit(Op.LOAD, Reg.ST, -3); // address
					Machine.emit(Op.LOAD, Reg.ST, -2); // int i loaded again
					Machine.emit(Prim.arrayref);
					Machine.emit(Prim.put);
					Machine.emit(Op.LOADL, 1); // add 1 to int
					Machine.emit(Prim.add);
					// visitExpression
					int jumpEval = Machine.nextInstrAddr();
					Machine.emit(Op.LOAD, Reg.ST, -1); // int i
					Machine.emit(Op.LOAD, Reg.ST, -3); // length
					Machine.emit(Prim.lt); // int i < length (length first, i second)
					Machine.emit(Op.JUMPIF, 1, Reg.CB, jumpDo); // if true run the code
					Machine.emit(Prim.puteol);
					Machine.patch(jumpAddr, jumpEval);
					// arrayref array addr a, element index i ==> ..., a[i]
					// put each character on the stack top
					// puteol
				} else {
					Machine.emit(Prim.putintnl);
				}
			} else if (ref.decl instanceof MethodDecl ) {
				if (((MethodDecl) ref.decl).isStatic) {
					Machine.emit(Op.POP, 1); // pop the pre-loaded values from stack
					patches.add(new Patch((MethodDecl) ref.decl, Machine.nextInstrAddr()));
					Machine.emit(Op.CALL, ref.decl.re.reg, ref.decl.re.offset); // offset from code base 
				} else {
					patches.add(new Patch((MethodDecl) ref.decl, Machine.nextInstrAddr()));
					Machine.emit(Op.CALLI, ref.decl.re.reg, ref.decl.re.offset); // offset from code base 
				}
			} else {
				if (ref.decl instanceof FieldDecl && ((FieldDecl) ref.decl).isStatic) {
					Machine.emit(Op.POP, 1);
					Machine.emit(Op.LOAD, ref.decl.re.reg, ref.decl.re.offset);
				} else if (ref.decl instanceof ArrayLengthExpr) {
					Machine.emit(Prim.arraylen);
				} else {
					Machine.emit(Op.LOADL, ref.decl.re.offset);
					if (!encode) {
						Machine.emit(Prim.fieldref);
					}
				}
			}
		} else {
			print = visitReference(ref.ref, ref.id.spelling + "." + arg);
			// neither out nor System load anything on the stack
			if (!(ref.decl instanceof MethodDecl) && print == null) {
				// TODO ???
				if (ref.decl instanceof FieldDecl && ((FieldDecl) ref.decl).isStatic) {
					// maybe remove the previous loading of a value on the stack?
					Machine.emit(Op.POP, 1);
					Machine.emit(Op.LOAD, ref.decl.re.reg, ref.decl.re.offset);
				} else {
					Machine.emit(Op.LOADL, ref.decl.re.offset);
					Machine.emit(Prim.fieldref);
				}
				
			} 
			
		}
		
		return print;
	}

	@Override
	public Object visitIdentifier(Identifier id, String arg) {
		
		return null;
	}

	@Override
	public Object visitOperator(Operator op, String arg) {
		switch (op.spelling) {
		case "||":
			Machine.emit(Prim.or);
			return null;
		case "&&":
			Machine.emit(Prim.and);
			return null;
		case "==":
			Machine.emit(Prim.eq);
			return null;
		case "!=":
			Machine.emit(Prim.ne);
			return null;
		case "<=":
			Machine.emit(Prim.le);
			return null;
		case "<":
			Machine.emit(Prim.lt);
			return null;
		case ">":
			Machine.emit(Prim.gt);
			return null;
		case ">=":
			Machine.emit(Prim.ge);
			return null;
		case "+":
			Machine.emit(Prim.add);
			return null;
		case "-":
			if (in(arg, "!binExpr")) {
				Machine.emit(Prim.sub);
				return null;
			}
			else {
				Machine.emit(Prim.neg);
				return null;
			}
		case "*":
			Machine.emit(Prim.mult);
			return null;
		case "/":
			Machine.emit(Prim.div);
			return null;
		case "!":
			Machine.emit(Prim.not);
			return null;
		default:
			return null;
		}
		
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, String arg) {
		Machine.emit(Op.LOADL, Integer.valueOf(num.spelling));
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, String arg) {
		if (bool.kind.equals(TokenKind.FALSE)) {
			Machine.emit(Op.LOADL, 0); // 0 is false for the machine
		} else {
			Machine.emit(Op.LOADL, 1); // 1 is true for the machine
		}
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nulll, String arg) {
		// null rep is 0???
		Machine.emit(Op.LOADL, 0);
		return null;
	}

	@Override
	public Object visitStringLiteral(StringLiteral sl, String arg) {
		// need to make an array of chars
		// Character.getNumericValue(char)
		//System.out.println(sl.spelling);
		Machine.emit(Op.LOADL, sl.spelling.length()); // length of new array
		Machine.emit(Prim.newarr); // leaves array address on the stack
		for (int i = 0; i < sl.spelling.length(); i++) {
			//Machine.emit(Op.PUSH, 1); // pushes the array address again so we don't eat it
			Machine.emit(Op.LOAD, Reg.ST, -1);
			Machine.emit(Op.LOADL, i); // offset from the array
			Machine.emit(Op.LOADL, (int) sl.spelling.charAt(i)); // the char as new int val
			Machine.emit(Prim.arrayupd); // eats three values and returns no result
		}
		
		// address of the char array left on the stack
		return null;
	}

}
