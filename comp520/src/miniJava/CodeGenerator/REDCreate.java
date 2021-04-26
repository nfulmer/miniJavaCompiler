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
	 * Two RED functions:
	 * 1) traverse all Declarations creating runtime entity descriptor (RED) for each declaration --> first traversal THEN run the main method
	 * 2) generate instructions in code store for each method in each class
	 */
	
	/*
	 * classes and other constants known at compile time are allocated on the stack in the global segment
	 * with runtime objects added dynamically on top of those
	 */
	
	AST tree;
	ErrorReporter reporter;
	int staticOffset;
	int offset;
	int main;
	int blockOffset;
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
							if (!((ArrayType) pd.type).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
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
			
			for (FieldDecl fd : cd.fieldDeclList) {
				visitFieldDecl(fd, arg);
			}
			
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
			// second pass idk -- static values???
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
		for (Statement s : stmt.sl) {
			visitStatement(s, arg + " !block");
		}
		Machine.emit(Op.POP, blockOffset);
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
			} /*else if (e instanceof ArrayLengthExpr) {
				visitArrayLengthExpr((ArrayLengthExpr) e, arg);
			} */
			
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
	
	/*
	 * encodeFetch
	 * encodeStore
	 * encodeMethodInvocation
	 */
	
	public void encodeFetch(Reference r) {
		// R  denotes a localDecl or fieldDecl, load value at Decl at stacktop
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
	
	public void encodeMethodInvocation(Reference r) {
		// R denotes a methoddecl, calli or call w needed args
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
		
		if (in(ref.id.spelling + "." + arg, "System.out.println")) {
			// alias for System.out.println
			Machine.emit(Prim.putintnl);
			return 1;
		} else if (ref.decl instanceof MethodDecl) {
			
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
			//System.out.println(ref.decl);
			if (ref.decl instanceof ClassDecl && ref.id.spelling.equals(ref.decl.name)) {
				//System.out.println(ref.id.spelling);
				//System.out.println(ref.decl.name);
				// Static reference
				// System.out.println(ref.decl.name);
				// don't need to actually do anything because the future things will get it
				// but what if it is an object? like a.p where p is static
				// TODO there would be excess arguments on the stack, right??
				// it seems like after a method, the stack cleans up after itself 
				// and if any values were overridden then it wouldn't matter
				// but what if there's like a super long qualified reference or something?
				// load random value?
				Machine.emit(Op.LOADL, 0); // placeholder value
			} else {
				//System.out.println(ref.decl);
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

			if (ref.decl instanceof MethodDecl && print == null) {
				if (((MethodDecl) ref.decl).isStatic) {
					Machine.emit(Op.POP, 1); // pop the pre-loaded values from stack
					patches.add(new Patch((MethodDecl) ref.decl, Machine.nextInstrAddr()));
					Machine.emit(Op.CALL, ref.decl.re.reg, ref.decl.re.offset); // offset from code base 
				} else {
					patches.add(new Patch((MethodDecl) ref.decl, Machine.nextInstrAddr()));
					Machine.emit(Op.CALLI, ref.decl.re.reg, ref.decl.re.offset); // offset from code base 
				}
			} else if (print == null){
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

	/*
	@Override
	public Object visitArrayLengthExpr(ArrayLengthExpr al, String arg) {
		visitReference(al.r, arg); // loads array address on the stack??
		Machine.emit(Prim.arraylen);
		return null;
	}*/

}
