package miniJava.SyntacticAnalyzer;

public class Token {
	
	public String spelling;
	public TokenKind kind;
	public SourcePosition posn;

	public Token(TokenKind kind, String spelling, SourcePosition posn) {
		this.spelling = spelling;
		this.kind = kind;
		this.posn = posn;
	}
	
}

/*The criterion for classifying tokens is simply this: all
tokens of the same kind can be freely interchanged without affecting the program's
phrase structure.

each token completely described by it's kind and spelling
*/