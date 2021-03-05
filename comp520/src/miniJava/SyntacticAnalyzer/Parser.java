package miniJava.SyntacticAnalyzer;

import miniJava.*;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;


public class Parser {

	private boolean trace;
	private ErrorReporter reporter;
	private Scanner scanner;
	private Token token;
	
	public Parser(Scanner scanner, ErrorReporter reporter, boolean trace) {
		this.scanner = scanner;
		this.reporter = reporter;
		this.trace = trace;
	}
	
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;
	}
	
	
	public AST parse() {
		token = scanner.scan();
		// TO DO: is this sufficient for when there's an error?
		AST parseTree = new ClassDecl("ERROR", new FieldDeclList(), new MethodDeclList(), token.posn);
		try {
			parseTree = parseProgram();
		} catch (SyntaxError e) {
			return null;
		} 
		ASTDisplay disp = new ASTDisplay();
		disp.showTree(parseTree);
		return parseTree;
	}
	
	void accept(Token expected){
		if(token.kind == expected.kind) {
			if (trace) {
				pTrace();
			}
			token = scanner.scan();
		} else {
			parseError("expecting '" + expected.kind + "' but found '" + token.kind + "' at token " + String.valueOf(expected.posn.getPosition()));
		}
		
	}
	
	private void parseError (String e) throws SyntaxError{
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}
	
	private void pTrace() {
		StackTraceElement[] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
		System.out.println();
	}
	
	//Program ::= (ClassDeclaration)* eot
	AST parseProgram() throws SyntaxError {
		Package program;
		ClassDeclList cdl = new ClassDeclList();
		while (token.kind != TokenKind.EOT) {
			cdl.add(parseClassDeclaration());
		}
		program = new Package(cdl, token.posn);
		return program;
	}
	
	//ClassDeclaration ::= class id { ( FieldDeclaration | MethodDeclaration )* }
	// massaged into: ClassDeclaration ::= class id { Visibility Access (VoidMethod | TypeFieldOrMethod)}
	ClassDecl parseClassDeclaration() throws SyntaxError {
		ClassDecl cd;
		accept(new Token(TokenKind.CLASS, "class", token.posn));
		
		String className = token.spelling; //TODO more rebust method of getting class name
		accept(new Token(TokenKind.ID, "?", token.posn));
		
		FieldDeclList fdl = new FieldDeclList();
		MethodDeclList mdl = new MethodDeclList();
		
		accept(new Token(TokenKind.LCURLY, "{", token.posn));
		MemberDecl md;
		while(token.kind != TokenKind.RCURLY) {
			md = parseFieldOrMethod();
			if (md instanceof FieldDecl) {
				fdl.add((FieldDecl) md);
			} else if (md instanceof MethodDecl) {
				mdl.add((MethodDecl) md);
			} else {
				//error??
			}
		}
		accept(new Token(TokenKind.RCURLY, "}", token.posn));
		
		cd = new ClassDecl(className, fdl, mdl, token.posn);
		return cd;
	}
	
	MemberDecl parseFieldOrMethod() {
		MemberDecl md;
		FieldDecl fd;
		boolean isPrivate = false;
		boolean isStatic = false;
		TypeDenoter td;
		String name;
		if (token.kind == TokenKind.VISIBILITY) {
			if (token.spelling == "private") {
				isPrivate = true;
			}
			accept(new Token(TokenKind.VISIBILITY, "?", token.posn));
		}
		if (token.kind == TokenKind.ACCESS) {
			isStatic = true;
			accept(new Token(TokenKind.ACCESS, "static", token.posn));
		}
		
		//TypeKinds are void, int, boolean, class, array, unsuported, and error 
		if (token.kind == TokenKind.VOID) {
			accept(new Token(TokenKind.VOID, "void", token.posn));
			td = new BaseType(TypeKind.VOID, token.posn);
			//parseVoidMethod();
		} else {
			td = parseType();
		}
		name = token.spelling;
		accept(new Token( TokenKind.ID, "?", token.posn));
		
		fd = new FieldDecl(isPrivate, isStatic, td, name, token.posn);
		
		switch(token.kind) {
		case LPAREN:
			accept(new Token(TokenKind.LPAREN, "(", token.posn));
			return parseRemainingMethod(fd);
		case SEMICOLON:
			accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
			if (fd.type.typeKind == TypeKind.VOID) {
				parseError("Cannot have a void type for field");
			}
			return fd;
		default:
			parseError("Invalid Term - expecting SEMICOLON or LPAREN but found " + token.kind);
			fd.type.typeKind = TypeKind.ERROR;
			return fd;
		}
	}
	
	/*MethodDeclaration ::= void id ( ParameterList? ) {Statement*}
	MethodDecl parseVoidMethod() throws SyntaxError {
		accept(new Token(TokenKind.VOID, "void"));
		accept(new Token( TokenKind.ID, "?"));
		accept(new Token(TokenKind.LPAREN, "("));
		return parseRemainingMethod();
	}*/
	
	/*massaged into: TypeFieldOrMethod ::= Type id ( ; | Method)
	void parseTypeFieldOrMethod() throws SyntaxError{
		
	}*/
	//Type ::= int | boolean | id | ( int | id ) [] 
	TypeDenoter parseType() throws SyntaxError {
		switch(token.kind) {
		case INT:
			accept(new Token( TokenKind.INT, "?", token.posn));
			if (token.kind == TokenKind.LBRACK) {
				accept(new Token( TokenKind.LBRACK, "[", token.posn));
				accept(new Token( TokenKind.RBRACK, "]", token.posn));
				return new ArrayType(new BaseType(TypeKind.INT, token.posn), token.posn);
			} else {
				return new BaseType(TypeKind.INT, token.posn);
			}
		case BOOLEAN:
			accept(new Token( TokenKind.BOOLEAN, "?", token.posn));
			return new BaseType(TypeKind.BOOLEAN, token.posn);
		case ID:
			Identifier id = new Identifier(token);
			accept(new Token( TokenKind.ID, "?", token.posn));
			if (token.kind == TokenKind.LBRACK) {
				accept(new Token( TokenKind.LBRACK, "[", token.posn));
				accept(new Token( TokenKind.RBRACK, "]", token.posn));
				return new ArrayType(new ClassType(id, token.posn), token.posn);
			} else {
				return new ClassType(id, token.posn);
			}
		default:
			parseError("Invalid Term - expecting INT, BOOLEAN, or ID but found " + token.kind);
			return new BaseType(TypeKind.ERROR, token.posn);
		}
	}
	
	//FieldDeclaration ::= Visibility Access Type id ;
	//MethodDeclaration ::= Visibility Access ( Type | void ) id ( ParameterList? ) {Statement*}
	//massaged into: TypeFieldOrMethod ::= Type id ( ; | Method)
	
	//RemainingMethod ::= ( ParameterList? ) {Statement*}
	MethodDecl parseRemainingMethod(FieldDecl fd) throws SyntaxError {
		ParameterDeclList pl;
		StatementList sl = new StatementList();
		if (token.kind != TokenKind.RPAREN) {
			pl = parseParameterList();
		} else {
			pl = new ParameterDeclList();
		}
		accept(new Token(TokenKind.RPAREN, ")", token.posn));
		accept(new Token(TokenKind.LCURLY, "{", token.posn));
		while (token.kind != TokenKind.RCURLY) {
			sl.add(parseStatement());
		}
		accept(new Token(TokenKind.RCURLY, "}", token.posn));
		return new MethodDecl(fd, pl, sl, token.posn);
	}
	
	//Type id ( , Type id )*
	ParameterDeclList parseParameterList() throws SyntaxError {
		ParameterDeclList pl = new ParameterDeclList();

		TypeDenoter td;
		String name;
		
		td = parseType();
		name = token.spelling;
		accept(new Token(TokenKind.ID, "?", token.posn));
		
		pl.add(new ParameterDecl(td, name, token.posn));
		
		while (token.kind == TokenKind.COMMA) {
			accept(new Token(TokenKind.COMMA, ",", token.posn));
			td = parseType();
			name = token.spelling;
			accept(new Token(TokenKind.ID, "?", token.posn));
			pl.add(new ParameterDecl(td, name, token.posn));
		}
		return pl;
	}
	
	/*Statement ::=
		 { Statement* }
		| Type id = Expression ;
		| Reference = Expression ;
		| Reference [ Expression ] = Expression ;
		| Reference ( ArgumentList? ) ;
		| return Expression? ;
		| if ( Expression ) Statement (else Statement)?
		| while ( Expression ) Statement*/
	/* massaged into Statement ::=
	 { Statement* }
	| TypeOrReference
	| return Expression? ;
	| if ( Expression ) Statement (else Statement)?
	| while ( Expression ) Statement*/
	Statement parseStatement() throws SyntaxError {
		//Statement stateAST;
		switch(token.kind) {
		case LCURLY:
			StatementList sl = new StatementList();
			accept(new Token(TokenKind.LCURLY, "{", token.posn));
			while (token.kind != TokenKind.RCURLY) {
				sl.add(parseStatement());
				// Statement sAST = parseStatement();
				//declAST = new BlockStmt() ; 
			}
			accept(new Token(TokenKind.RCURLY, "}", token.posn));
			return new BlockStmt(sl, token.posn);
		case RETURN:
			accept(new Token(TokenKind.RETURN, "return", token.posn));
			Expression e = null;
			if (token.kind != TokenKind.SEMICOLON) {
				e = parseExpression();
				//Expression eAST = parseExpression();
			}
			//TO DO What if no expression, just semi-colon?
			accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
			return new ReturnStmt(e, token.posn);
		case IF:
			accept(new Token(TokenKind.IF, "if", token.posn));
			accept(new Token(TokenKind.LPAREN, "(", token.posn));
			Expression cond = parseExpression();
			accept(new Token(TokenKind.RPAREN, ")", token.posn));
			Statement thenState = parseStatement();
			Statement elseState = null;
			if (token.kind == TokenKind.ELSE) {
				accept(new Token(TokenKind.ELSE, "else", token.posn));
				elseState = parseStatement();
				// TO DO: what if no else statement??
			}
			return new IfStmt(cond, thenState, elseState, token.posn);
		case WHILE:
			accept(new Token(TokenKind.WHILE, "while", token.posn));
			accept(new Token(TokenKind.LPAREN, "(", token.posn));
			cond = parseExpression();
			accept(new Token(TokenKind.RPAREN, ")", token.posn));
			Statement body = parseStatement();
			return new WhileStmt(cond, body, token.posn);
		default:
			return parseTypeOrReference();
			//CallStmt
			//AssignStmt
			//IxAssignStmt
			//VarDeclStmt
		}
	}
	//Type ::= int | boolean | id | ( int | id ) [] 
	/*Reference ::= (id | this) Reference'
	 * Reference'::= .idReference' | e */
	/* Type id = Expression ;
	| Reference = Expression ;
	| Reference [ Expression ] = Expression ;
	| Reference ( ArgumentList? ) ; */
	
	//CallStmt
	//AssignStmt
	//IxAssignStmt
	//VarDeclStmt ---
	Statement parseTypeOrReference() throws SyntaxError {
		TypeDenoter t;
		String name;
		Expression e;
		
		Reference ref;
		
		switch(token.kind) {
		case INT:
		case BOOLEAN: 
			t = parseType();
			name = token.spelling;
			accept(new Token(TokenKind.ID, "?", token.posn));
			accept(new Token(TokenKind.EQUALS, "=", token.posn));
			e = parseExpression();
			accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
			return new VarDeclStmt(new VarDecl(t,name,token.posn), e, token.posn);
		case ID:
			//Type id = Expression ;
			Identifier id = new Identifier(token);
			accept(new Token(TokenKind.ID, ";", token.posn));
			if (token.kind == TokenKind.LBRACK) {
				accept(new Token(TokenKind.LBRACK, "[", token.posn));
				if (token.kind != TokenKind.RBRACK) {
				// reference [expression] = expression
				// id[expression] = expression
					Expression i = parseExpression();
					accept(new Token(TokenKind.RBRACK, "]", token.posn));
					accept(new Token(TokenKind.EQUALS, "=", token.posn));
					Expression iae = parseExpression();
					accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
					return new IxAssignStmt(new IdRef(id, token.posn), i, iae, token.posn);
				} else {
					// id[] id = expression;
					t = new ArrayType(new ClassType(id, token.posn), token.posn);
					accept(new Token(TokenKind.RBRACK, "]", token.posn));
					name = token.spelling;
					accept(new Token(TokenKind.ID, ";", token.posn));
					accept(new Token(TokenKind.EQUALS, "=", token.posn));
					e = parseExpression();
					accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
					return new VarDeclStmt(new VarDecl(t,name,token.posn), e, token.posn);
				}
			}
			//case id id = expression id; has to be type id = expression;
			if (token.kind == TokenKind.ID) {
				t = new ClassType(id, token.posn);
				name = token.spelling;
				accept(new Token(TokenKind.ID, "?", token.posn));
				accept(new Token(TokenKind.EQUALS, "=", token.posn));
				e = parseExpression();
				accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
				return new VarDeclStmt(new VarDecl(t,name,token.posn), e, token.posn);
			} else {
				//deals with case id.
				ref = new IdRef(id, token.posn);
				ref = parseReferencePrime(ref);
			}
			break;
		default:
			ref = parseReference();
		}
		switch(token.kind) {
		case LPAREN:
			ExprList el;
			accept(new Token(TokenKind.LPAREN, "(", token.posn));
			if (token.kind != TokenKind.RPAREN) {
				el = parseArgumentList();
			} else {
				el = new ExprList();
			}
			accept(new Token(TokenKind.RPAREN, ")", token.posn));
			accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
			return new CallStmt(ref, el, token.posn);
		case LBRACK:
			accept(new Token(TokenKind.LBRACK, "[", token.posn));
			Expression i = parseExpression();
			accept(new Token(TokenKind.RBRACK, "]", token.posn));
			accept(new Token(TokenKind.EQUALS, "=", token.posn));
			Expression iae = parseExpression();
			accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
			return new IxAssignStmt(ref, i, iae, token.posn);
		case EQUALS:
			accept(new Token(TokenKind.EQUALS, "=", token.posn));
			Expression ae = parseExpression();
			accept(new Token(TokenKind.SEMICOLON, ";", token.posn));
			return new AssignStmt(ref, ae, token.posn);
		default:
			parseError("Invalid Term - expecting LPAREN, LBRACK, or EQUALS but found " + token.kind);
			return new AssignStmt(null, null, token.posn);
			// TODO sufficient for error?
		}
	}
	 
	// TO DO: unary vs binary minus operation
	/* ---> stratified grammar
	 * Expression :: = T ( || T )* 
	 * T ::= F ( && F)* 
	 * F ::= G ((==G) | (!=G))*
	 * G ::= X ( (<=X) | (<X) | >X | >=X)*
	 * X ::= Y ( + Y | - Y)*
	 * Y ::= Z (*Z | /Z)*
	 * Z ::= W (!W | -W)* 
	 * Z ::= Reference
 | Reference [ Expression ]
| Reference ( ArgumentList? )
| unop Expression | MINUS Expression
| Expression binop Expression
| ( Expression )
| num | true | false
| new ( id () | int [ Expression ] | id [ Expression ] )
	 */
	Expression parseExpression() {
		Expression e = parseT();
		Operator o;
		while (token.spelling == "||") {
			o = new Operator(token);
			accept(new Token(TokenKind.BINOP, "||", token.posn));
			e = new BinaryExpr(o, e, parseT(), token.posn);
		}
		return e;
	}
	
	//T ::= F ( && F)* 
	Expression parseT() {
		Expression e = parseF();
		Operator o;
		while (token.spelling == "&&") {
			o = new Operator(token);
			accept(new Token(TokenKind.BINOP, "&&", token.posn));
			e = new BinaryExpr(o, e, parseF(), token.posn);
		}
		return e;	
	}
	
	//F ::= G ((==G) | (!=G))*
	Expression parseF() {
		Expression e = parseG();
		Operator o;
		while (token.spelling == "==" || token.spelling == "!=") {
			o = new Operator(token);
			accept(new Token(TokenKind.BINOP, "?", token.posn));
			e = new BinaryExpr(o, e, parseG(), token.posn);
		}
		return e;	
	}
	
	//G ::= X ( (<=X) | (<X) | >X | >=X)*
	Expression parseG() {
		Expression e = parseX();
		Operator o;
		while (token.spelling == "<=" || token.spelling == "<" 
				||token.spelling == ">=" || token.spelling == ">" ) {
			o = new Operator(token);
			accept(new Token(TokenKind.BINOP, "?", token.posn));
			e = new BinaryExpr(o, e, parseX(), token.posn);
		}
		return e;	
	}
	
	//X ::= Y ( + Y | - Y)*
	Expression parseX() {
		Expression e = parseY();
		Operator o;
		while (token.spelling == "+" || token.spelling == "-" ) {
			o = new Operator(token);
			if (token.spelling == "-") {
				accept(new Token(TokenKind.MINUS, "-", token.posn));
			} else {
				accept(new Token(TokenKind.BINOP, "+", token.posn));
			}
			e = new BinaryExpr(o, e, parseY(), token.posn);
		}
		return e;
	}
	
	//Y ::= Z (*Z | /Z)*
	Expression parseY() {
		Expression e = parseZ();
		Operator o;
		while (token.spelling == "*" || token.spelling == "/") {
			o = new Operator(token);
			accept(new Token(TokenKind.BINOP, "?", token.posn));
			e = new BinaryExpr(o, e, parseZ(), token.posn);
		}
		return e;	
	}
	
	/*Z ::= Reference
			 | Reference [ Expression ]
					 | Reference ( ArgumentList? )
					 | unop Expression | MINUS Expression
					 | ( Expression )
					 | num | true | false
					 | new ( id () | int [ Expression ] | id [ Expression ] )
					 	 */
	Expression parseZ() throws SyntaxError {
		Expression exp;
		Operator op;
		Expression subExp;
		switch(token.kind) {
		case MINUS:
			// TO DO: check that token.minus is valid for operator
			op = new Operator(token);
			accept(new Token(TokenKind.MINUS, "-", token.posn));
			// while ***collect all the negatives and then return that
			subExp = parseZ();
			return new UnaryExpr(op, subExp, token.posn);
		case UNOP:
			op = new Operator(token);
			accept(new Token(TokenKind.UNOP, "?", token.posn));
			subExp = parseZ();
			// the issue is that the sub exp might include other operations, so what we want to do is collect only other parseZ's??
			return new UnaryExpr(op, subExp, token.posn); 
		case LPAREN:
			// TO DO: is this correct???
			accept(new Token(TokenKind.LPAREN, "(", token.posn));
			exp = parseExpression();
			accept(new Token(TokenKind.RPAREN, ")", token.posn));
			return exp;
		case NUM:
			exp = new LiteralExpr(new IntLiteral(token), token.posn);
			accept(new Token(TokenKind.NUM, "?", token.posn));
			return exp;
		case TRUE:
			exp = new LiteralExpr(new BooleanLiteral(token), token.posn);
			accept(new Token(TokenKind.TRUE, "?", token.posn));
			return exp;
		case FALSE:
			exp = new LiteralExpr(new BooleanLiteral(token), token.posn);
			accept(new Token(TokenKind.FALSE, "?", token.posn));
			return exp;
		case NEW:
			accept(new Token(TokenKind.NEW, "new", token.posn));
			switch(token.kind) {
			case ID:
				Identifier id = new Identifier(token);
				accept(new Token(TokenKind.ID, "?", token.posn));
				switch(token.kind) {
				case LPAREN:
					accept(new Token(TokenKind.LPAREN, "(", token.posn));
					accept(new Token(TokenKind.RPAREN, ")", token.posn));
					return new NewObjectExpr(new ClassType(id, token.posn), token.posn);
				case LBRACK:
					accept(new Token(TokenKind.LBRACK, "[", token.posn));
					subExp = parseExpression();
					accept(new Token(TokenKind.RBRACK, "]", token.posn));
					return new NewArrayExpr(new ClassType(id, token.posn), subExp, token.posn);
				default:
					parseError("Invalid Term - expecting LPAREN or LBRACK but found " + token.kind);
					return new RefExpr(null, token.posn);
				}
			case INT:
				accept(new Token(TokenKind.INT, "int", token.posn));
				accept(new Token(TokenKind.LBRACK, "[", token.posn));
				subExp = parseExpression();
				accept(new Token(TokenKind.RBRACK, "]", token.posn));
				return new NewArrayExpr(new BaseType(TypeKind.INT, token.posn), subExp, token.posn);
			default:
				parseError("Invalid Term - expecting INT or ID but found " + token.kind);
				return new RefExpr(null, token.posn);
			}
		case ID:
		case THIS:
			Reference r = parseReference();
			if (token.kind == TokenKind.LBRACK) {
				accept(new Token(TokenKind.LBRACK, "[", token.posn));
				subExp = parseExpression();
				accept(new Token(TokenKind.RBRACK, "]", token.posn));
				return new IxExpr(r, subExp, token.posn);
			} else if (token.kind == TokenKind.LPAREN) {
				accept(new Token(TokenKind.LPAREN, "(", token.posn));
				ExprList el;
				if (token.kind != TokenKind.RPAREN) {
					el = parseArgumentList();
				} else {
					el = new ExprList();
				}
				accept(new Token(TokenKind.RPAREN, ")", token.posn));
				return new CallExpr(r, el, token.posn);
			} else {
				return new RefExpr(r, token.posn);
			}
		default:
			parseError("Invalid Term - not expecting " + token.kind);
			return new RefExpr(null, token.posn);
			//TODO sufficient for error?
		}
	}
	
	/*Reference ::= (id | this) Reference'
	 * Reference' ::= .idReference' | e
	 */
	//BaseRef (IdRef, ThisRef) , QualRef
	Reference parseReference() throws SyntaxError {
		Reference r = null;
		switch(token.kind) {
		case ID:
			r = new IdRef(new Identifier(token), token.posn);
			accept(new Token(TokenKind.ID, "?", token.posn));
			break;
		case THIS:
			r = new ThisRef(token.posn);
			accept(new Token(TokenKind.THIS, "this", token.posn));
			break;
		default:
			parseError("Invalid Term - expecting ID or THIS but found " + token.kind);
			return new IdRef(null, token.posn);
		}
		return parseReferencePrime(r);
	} 
	
	//Reference'::= .idReference' | e
	Reference parseReferencePrime(Reference r) throws SyntaxError {
		if (token.kind == TokenKind.PERIOD) {
			accept(new Token(TokenKind.PERIOD, ".", token.posn));
			Identifier id = new Identifier(token);
			accept(new Token(TokenKind.ID, "?", token.posn));
			return parseReferencePrime(new QualRef(r, id, token.posn));
		} else {
			return r;
		}
	}
	
	//ArgumentList ::= Expression ( , Expression )*
	ExprList parseArgumentList() throws SyntaxError{
		ExprList el = new ExprList();
		el.add(parseExpression());
		while (token.kind == TokenKind.COMMA) {
			accept(new Token(TokenKind.COMMA, ",", token.posn));
			el.add(parseExpression());
		}
		return el;
	}

}

