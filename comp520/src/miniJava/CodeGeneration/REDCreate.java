package miniJava.CodeGeneration;

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
	int offset = 0;

	/*
	 * encodeFetch and encodeAssign
	 */
	
	// call for static methods, calli for instance methods, calld for dynamic method invocation

	public REDCreate(AST tree, ErrorReporter reporter) {
		this.tree = tree;
		this.reporter = reporter;
		
		visitPackage((Package) tree, "");
		
		
		/*Machine.emit(Op.LOADL,0);
		Machine.emit(Op.PUSH, 1);
		Machine.emit(Op.LOADL,1);
		Machine.emit(Op.STORE,Machine.Reg.SB,0);
		Machine.emit(Op.LOADL,2);
		Machine.emit(Op.LOADL,3);
		Machine.emit(Prim.add);
		Machine.emit(Op.STORE,Machine.Reg.SB,0);

		//Machine.emit(Op.CALL, Machine.Reg.CP.ordinal());
		//Machine.emit(Prim.newarr);
		Machine.emit(Op.HALT, 0, 0, 0);*/ 
		
	}
	
	private void REDError (String e) throws REDError{
		reporter.reportError(e);
		throw new REDError();
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
		
		// what constitutes "unique?", can I have a private static void main(String[] args) method?
		
		MethodDecl main = null;

		
		// first pass to go through and generate runtime entities for each (?) of the declarations
		for (ClassDecl c : prog.classDeclList) {
			visitClassDecl(c, arg);
					
			for (MethodDecl md : c.methodDeclList) {
				
				// TODO: wtf is the displacement?? runtime entity
				
				if (!md.isPrivate && md.isStatic 
						&& md.type.typeKind.equals(TypeKind.VOID) 
						&& md.name.equals("main")) {
					if (md.parameterDeclList.size() > 1 || md.parameterDeclList.size() == 0) {
						//REDError("Error! Public static void main method needs one input argument!");
						continue;
					} else {
						ParameterDecl pd = md.parameterDeclList.get(0);
						if (!pd.type.typeKind.equals(TypeKind.ARRAY)) {
							//REDError("Error! Public static void main method needs String[] input argument type!");
							continue;
						} else {
							if (!((ArrayType) pd.type).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
								//REDError("Error! Public static void main method needs String[] input argument type!");
								continue;
							}
							
							/* TODO: confirm it doesn't have to be called args */
							if (!pd.name.equals("args")) {
								//REDError("Error! Public static void main method needs 'args' input argument!");
								continue;
							} 
							
							if ((main != null)) {
								REDError("Error! Cannot have mutiple public static void main methods!");
							} else {
								//Machine.emit(Op.LOADL, 0);
								
								// testing that mJAM works
								//Machine.emit(Op.LOADL, 15); 
								//Machine.emit(Prim.putintnl); // a putintnl instruction in mJAM w value 15 on stack will produce >>> 15
								
								/*
								 * emit code to call main and halt on return
								 * - code starts at location 0 in code store
								 * - create empty args array on heap
								 * - call main (address L11 must be patched)
								 * - on return halt w code 0
								 */
								//Machine.emit(Op.CALL, 0);
								// need to add args onto the stack??
								
								//Machine.emit(op);
								main = md;
							}
						}
						
					}
				}
			}
		}
		
		if (main == null) {

			REDError("Error! Need a unique public static void main method for the program!");
		} else {
			Machine.emit(Op.LOADL, 0);
			visitMethodDecl(main, arg); // generate code from main method and that's what's relevant
			Machine.emit(Op.HALT, 0, 0, 0); // run [[c]] = execute [[C]] HALT
		}
		
		/*for (ClassDecl c : prog.classDeclList) {
			visitClassDecl(c, arg);
		}*/
		
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, String arg) {
		cd.re = new UnknownValue(cd.fieldDeclList.size(), Machine.Reg.ST.ordinal()); // second one needs changed
		int displacement = 0;
		for (FieldDecl fd : cd.fieldDeclList) {
			visitFieldDecl(fd, String.valueOf(displacement));
			displacement++;
		}
		
		for (MethodDecl md : cd.methodDeclList) {
			visitMethodDecl(md, arg);
		}
		return cd.fieldDeclList.size();
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, String arg) {
		// what about local values???
		fd.re = new UnknownValue(1, Integer.valueOf(arg));
		return 1;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, String arg) {
		if (md.re == null) {
			//System.out.println(Reg.CP.ordinal());
			md.re = new UnknownValue(1, Reg.CP.ordinal()); // ??? idk slide 8 of miniJavaCodeGenExample
		} else {
			// arguments are added to the stack top before we execute the function
			// we are actually executing


			for (ParameterDecl pd : md.parameterDeclList) {
				//visitParameterDecl(pd, arg);
			}
			
			//System.out.println(((UnknownValue) md.re).address);
			
			//Machine.emit(Op.CALL, 1);
			
			//Machine.emit(Op.CALL, ((UnknownValue) md.re).address); // in machine: goes back to whatever 
			// address is called w call for the code pointer to continue executing at
			
			for (Statement s: md.statementList) {
				visitStatement(s, arg);
			}
			
			//Machine.emit(Op.RETURN, 0, 0, 0); 
			// pop off the array argument
		}
		
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, String arg) {
		//pd.re = new UnknownValue(1, Machine.Reg.ST.ordinal()); // ???
		switch (pd.type.typeKind) {
		case ARRAY:
			//Machine.emit(Prim.newarr);
			break;
		case INT:
		case BOOLEAN:
			//Machine.emit(Op.PUSH, 1);
			break;
		case CLASS:
			//Machine.emit(Prim.newobj);
			break;
		default:
			// TODO: other typekinds are void, null, unsupported and error
		}
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, String arg) {
		// 
		// TODO Auto-generated method stub
		//Machine.emit(Op.LOADL, 1); // size of new object, in this case int
		// if array call newarr
		// if object call newobj
		
		// put a new object on the stack
		offset += 1;
		Machine.emit(Op.PUSH, 1);
		
		// offset from LB
		// System.out.println(Machine.Reg.LB.ordinal());
		//decl.re = new UnknownValue(1, Machine.Reg.ST.ordinal() - Machine.Reg.LB.ordinal()); // displacement from the lobal base (LB)
		
		decl.re = new UnknownValue(1, offset - 1);
		return 1;
	}

	@Override
	public Object visitBaseType(BaseType type, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, String arg) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Object visitStatement(Statement s, String arg){
			// redirect

		// execute statement, updating variables, 
		// no change in frame size on termination except varDeclStmt which extends frame by 1
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
		for (Statement s : stmt.sl) {
			visitStatement(s, arg);
		}
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, String arg) {
		// TODO Auto-generated method stub
		// extend stack by 1
		/*Machine.emit(Op.LOADL, Reg.LB, 0);
		visitVarDecl(stmt.varDecl, arg);
		visitExpression(stmt.initExp, arg);// expression puts result on top of stack
		Machine.emit(Op.STORE, 0);
		// test
		Machine.emit(Op.LOADL, Reg.LB, 0);
		Machine.emit(Prim.putintnl);*/ 

		
		visitVarDecl(stmt.varDecl, arg);
		visitExpression(stmt.initExp, arg);
		
		Machine.emit(Op.STORE, Machine.Reg.LB, ((UnknownValue) stmt.varDecl.re).address); // displacement from LB
		// store expression from stack into vardecl
		//Machine.emit(Op.STORE, Machine.Reg.LB, 0);
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, String arg) {
		// visitExpression puts the value on stack top 
		// then encodeStore pops the value from ST and stores in ref
		// TODO
		if (stmt.ref instanceof QualRef) {
			encodeStore(stmt.ref);
			visitExpression(stmt.val, arg);
			Machine.emit(Prim.fieldupd);
		} else {
			visitExpression(stmt.val, arg);
			encodeStore(stmt.ref);
		}
		return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, String arg) {
		// TODO Auto-generated method stub
		// what do we do w the stmt.argList??
		
		//encodeMethodInvocation(stmt.methodRef);
		
		// FOR NOW: all of our call expressions are going to be System.out.println
		for (Expression ex : stmt.argList) {
			visitExpression(ex, arg);
		}
		visitReference(stmt.methodRef, " !method");
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, String arg) {
		// TODO Auto-generated method stub
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
			} else if (e instanceof ArrayLengthExpr) {
				visitArrayLengthExpr((ArrayLengthExpr) e, arg);
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
		visitExpression(expr.left, arg);
		visitExpression(expr.right, arg);
		
		visitOperator(expr.operator, arg + " !binExpr");
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, String arg) {
		// TODO Auto-generated method stub
		visitReference(expr.ref, "");
		return null;
	}

	@Override
	public Object visitIxExpr(IxExpr expr, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, String arg) {
		// TODO Auto-generated method stub\	
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
		// TODO Auto-generated method stub
		Machine.emit(Op.LOADL, -1); // need to change if i implement inheritance
		Machine.emit(Op.LOADL, expr.classtype.className.decl.re.size);
		Machine.emit(Prim.newobj);
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, String arg) {
		// TODO Auto-generated method stub
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
		
		if (r instanceof IdRef) {
			Machine.emit(Op.STORE, Reg.LB, ((UnknownValue) r.decl.re).address);
		} else if (r instanceof QualRef) {
			visitQRef((QualRef) r, " !encode");
		}
		
	}
	
	public void encodeMethodInvocation(Reference r) {
		// R denotes a methoddecl, calli or call w needed args
	}

	@Override
	public Object visitThisRef(ThisRef ref, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, String arg) {
		// TODO Auto-generated method stub
		boolean encode = false;
		if (in(arg, "!encode")) {
			encode = true;
		}
		//System.out.println(arg);
		String[] argss = arg.split("\\s+");
		arg = argss[0];
		
		if (arg.equals("")) {
			//loads address on stack
			Machine.emit(Op.LOADL, ((UnknownValue) ref.decl.re).address);
			// loads value from address on stack
			Machine.emit(Op.LOADI); 
		} else {
			// qualified reference
			arg = ref.id.spelling + "." + arg;
			if (arg.equals("System.out.println")) {
				Machine.emit(Prim.putintnl);
			} else {
				Machine.emit(Op.LOAD, Reg.LB, ((UnknownValue) ref.decl.re).address);
				/*
				String[] args = arg.split("\\.");
				// have to loadl displacement of x from a 
				switch (ref.decl.type.typeKind) {
				case CLASS:
					ClassDecl cd = (ClassDecl) ((ClassType) ref.decl.type).className.decl;
					for (FieldDecl fd : cd.fieldDeclList) {
						if (fd.name.equals(args[1])) {
							Machine.emit(Op.LOADL, ((UnknownValue) fd.re).address);
							
							if (!encode || args.length > 2) {
								Machine.emit(Prim.fieldref);
							}
							break;
							// TODO: multiple args
							// we actually need to load the field reference from the first object onto stack (will have heap address)
							// then we need to get the displacement 
							// THEN we need to put the value we want to store then we can update it
						}
					}
				default:
				}
				if (args.length == 2) {
					//Machine.emit(Prim.fieldref); 
					// now we have the location of the second object on the stack
					
				}
				
			*/	
			}
		}
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, String arg) {
		// TODO Auto-generated method stub
		boolean encode = false;
		boolean method = false;
		if (in(arg, "!method")) {
			method = true;
		}
		if (in(arg, "!encode")) {
			encode = true;
		}
		System.out.println(arg);
		String[] argss = arg.split("\\s+");
		//System.out.println(ref.decl.type);
		if (argss[0].equals("")) { // no more qualifiers afterward
			visitReference(ref.ref, ref.id.spelling + arg);
			if (!method) {
				Machine.emit(Op.LOADL, ((UnknownValue) ref.decl.re).address);
			}
			if (!encode) {
				Machine.emit(Prim.fieldref);
			}
		} else {
			visitReference(ref.ref, ref.id.spelling + "." + arg);
			if (!method) {

				Machine.emit(Op.LOADL, ((UnknownValue) ref.decl.re).address);
				Machine.emit(Prim.fieldref);
			}
			
		} 
		
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, String arg) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		// null rep is 0???
		Machine.emit(Op.LOADL, 0);
		return null;
	}

	@Override
	public Object visitArrayLengthExpr(ArrayLengthExpr al, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
