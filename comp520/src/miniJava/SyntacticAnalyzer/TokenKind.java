package miniJava.SyntacticAnalyzer;

public enum TokenKind {
	NULL, TRUE, FALSE, ERROR, ID, MINUS, BINOP, PERIOD, NUM, 
	NEW, UNOP, THIS, EQUALS, LBRACK, RBRACK, IF, INT, ELSE, 
	WHILE, BOOLEAN, RETURN, EOT, LPAREN, SEMICOLON, RPAREN, 
	LCURLY, RCURLY, VISIBILITY, ACCESS, CLASS, VOID, COMMA,
	// added length tokenkind for pa4
	LENGTH;
}
