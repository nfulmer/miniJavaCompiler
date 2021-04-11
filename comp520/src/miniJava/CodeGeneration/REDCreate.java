package miniJava.CodeGeneration;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;

public class REDCreate implements Visitor<String, Object> {
	
	AST tree;
	ErrorReporter reporter;

	public REDCreate(AST tree, ErrorReporter reporter) {
		this.tree = tree;
		this.reporter = reporter;
		
		visitPackage((Package) tree, "");
	}
	
	private void REDError (String e) throws REDError{
		reporter.reportError(e);
		throw new REDError();
	}
	
	class REDError extends Error {
		private static final long serialVersionUID = 1L;
	}

	@Override
	public Object visitPackage(Package prog, String arg) {
		
		// what constitutes "unique?", can I have a private static void main(String[] args) method?
		
		boolean uniquePSVM = false;
		for (ClassDecl c : prog.classDeclList) {
			for (MethodDecl md : c.methodDeclList) {
				if (!md.isPrivate && md.isStatic 
						&& md.type.typeKind.equals(TypeKind.VOID) 
						&& md.name.equals("main")) {
					if (md.parameterDeclList.size() > 1 || md.parameterDeclList.size() == 0) {
						REDError("Error! Public static void main method needs one input argument!");
					} else {
						ParameterDecl pd = md.parameterDeclList.get(0);
						if (!pd.type.typeKind.equals(TypeKind.ARRAY)) {
							REDError("Error! Public static void main method needs String[] input argument type!");
						} else {
							if (!((ArrayType) pd.type).eltType.typeKind.equals(TypeKind.UNSUPPORTED)) {
								REDError("Error! Public static void main method needs String[] input argument type!");
							}
						}
						/* TODO: confirm it doesn't have to be called args
						if (!pd.name.equals("args")) {
							REDError("Error! Public static void main method needs 'args' input argument!");
						} */
					}
					if (uniquePSVM == true) {
						REDError("Error! Cannot have mutiple public static void main methods!");
					} else {
						uniquePSVM = true;
					}
				}
			}
		}
		
		if (!uniquePSVM) {
			REDError("Error! Need a unique public static void main method for the program!");
		}
		
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, String arg) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public Object visitBlockStmt(BlockStmt stmt, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, String arg) {
		// TODO Auto-generated method stub
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
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, String arg) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitOperator(Operator op, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nulll, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitArrayLengthExpr(ArrayLengthExpr al, String arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
