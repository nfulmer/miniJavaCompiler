package miniJava.SyntacticAnalyzer;

import java.io.*;

import miniJava.*;

public class Scanner {
	
	private ErrorReporter reporter;
	private FileReader fileReader;
	private StringReader stringReader;
	//private ErrorReporter reporter;
	
	private boolean debug;
	private char currentChar;
	private StringBuilder currentSpelling;
	private boolean eot = false; 
	private SourcePosition sposn;
	
	public Scanner(ErrorReporter reporter, String fileName, boolean debug) {
		this.reporter = reporter;
		this.debug = debug;
		this.sposn = new SourcePosition(1);
		if (debug) {
			System.out.println(fileName);
			stringReader = new StringReader(fileName);
		} else {
		try {
			fileReader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}}
		readChar();
	}
	
	public Token scan() {
		while (currentChar == ' ' || currentChar == '\r' || currentChar == '\n' || currentChar == '\t') {
		// whitespace is limited to spaces, tabs (\t), newlines (\n) and carriage returns (\r)
			if (currentChar == '\n') {
				sposn.advancePosition();
			}
			readChar();
		}
		currentSpelling = new StringBuilder();
		Token sc = scanToken();
		return sc;
	}

	/*relational operators: > < == <= >= !=
logical operators: && || !
arithmetic operators: + - * / */
// (id) is formed from a sequence of letters, digits, and the underscore character, and must start with a letter.
	// TODO '-' case bc both unop and binop!!!
	public Token scanToken(){ 
		if (eot) {
			return new Token(TokenKind.EOT, "?", sposn);
		}
		switch(currentChar) {
		case ',':
			readChar();
			return new Token(TokenKind.COMMA, ",", sposn);
		case '.':
			readChar();
			return new Token(TokenKind.PERIOD, ".", sposn);
		case '|':
			readChar();
			if (currentChar == '|') {
				readChar();
				return new Token(TokenKind.BINOP, "||", sposn);
			} else {
				scanError("Unrecognized character '|' in input");
				return new Token(TokenKind.ERROR, "?",sposn);
			}
		case '&':
			readChar();
			if (currentChar == '&') {
				readChar();
				return new Token(TokenKind.BINOP, "&&", sposn);
			} else {
				scanError("Unrecognized character '&' in input");
				return new Token(TokenKind.ERROR, "?", sposn);
			}
		case '=':
			readChar();
			if (currentChar == '=') {
				readChar();
				return new Token(TokenKind.BINOP, "==", sposn);
			} else {
				return new Token(TokenKind.EQUALS, "=", sposn);
			}
		case '!':
			readChar();
			if (currentChar == '=') {
				readChar();
				return new Token(TokenKind.BINOP, "!=", sposn);
			} else {
				return new Token(TokenKind.UNOP, "!", sposn);
			}
		case '<':
			readChar();
			if (currentChar == '=') {
				readChar();
				return new Token(TokenKind.BINOP, "<=", sposn);
			} else {
				return new Token(TokenKind.BINOP, "<", sposn);
			}
		case '>':
			readChar();
			if (currentChar == '=') {
				readChar();
				return new Token(TokenKind.BINOP, ">=", sposn);
			} else {
				return new Token(TokenKind.BINOP, ">", sposn);
			}
		case '-':
			readChar();
			return new Token(TokenKind.MINUS, "-", sposn);
		case '/':
			readChar();
			if (currentChar == '/') {
				readChar();
				readSingleComment();
				return this.scan();
			} else if (currentChar == '*') {
				readChar();
				readMultiComment();
				return this.scan();
			} else {
				return new Token(TokenKind.BINOP, "/", sposn);
			}
		case '*':
			readChar();
			return new Token(TokenKind.BINOP, "*", sposn);
		case '+':
			readChar();
			return new Token(TokenKind.BINOP, "+", sposn);
		case '$':
			readChar();
			return new Token(TokenKind.EOT, "$", sposn);
		case '(':
			readChar();
			return new Token(TokenKind.LPAREN, "(", sposn) ;
		case ')':
			readChar();
			return new Token(TokenKind.RPAREN, ")", sposn) ;
		case '[':
			readChar();
			return new Token(TokenKind.LBRACK, "[", sposn) ;
		case ']':
			readChar();
			return new Token(TokenKind.RBRACK, "]", sposn) ;
		case '{':
			readChar();
			return new Token(TokenKind.LCURLY, "{", sposn) ;
		case '}':
			readChar();
			return new Token(TokenKind.RCURLY, "}", sposn) ;
		case ';':
			readChar();
			return new Token(TokenKind.SEMICOLON, ";", sposn) ;
		default:
			return scanMultiple();
		}
	}
	
	void readSingleComment() {
		while (currentChar != '\n' && !eot){
			readChar();
		}
		if (currentChar == '\n') {
			sposn.advancePosition();
			readChar();
		}
	}
	
	void readMultiComment() {
		while (currentChar != '*' && !eot) {
			if (currentChar == '\n') {
				sposn.advancePosition();
			}
			readChar();
		}
		if (currentChar == '*') {
			readChar();
			if (currentChar == '/') {
				readChar();
				return;
			} else {
				readMultiComment();
			}
		} else if (eot) {
			scanError("Multiline comment not ended before the end of the file! ");
		}
	}
	
	//https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java
	//leading underscore is fine
	Token scanMultiple() {
		if (Character.isLetter(currentChar)) {
			while(Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_') {
				currentSpelling.append(currentChar);
				readChar();
			}
			switch(currentSpelling.toString()) {
			// added array length for pa4 --> jk
			/*case "length":
				return new Token(TokenKind.LENGTH, "length", sposn);*/
			case "String":
				return new Token(TokenKind.STRING, "String", sposn);
			case "null":
				return new Token(TokenKind.NULL, "null", sposn);
			case "true":
				return new Token(TokenKind.TRUE, "true", sposn) ;
			case "false":
				return new Token(TokenKind.FALSE, "false", sposn) ;
			case "void":
				return new Token(TokenKind.VOID, "void", sposn) ;
			case "return":
				return new Token(TokenKind.RETURN, "return", sposn) ;
			case "this":
				return new Token(TokenKind.THIS, "this", sposn) ;
			case "while":
				return new Token(TokenKind.WHILE, "while", sposn) ;
			case "if":
				return new Token(TokenKind.IF, "if", sposn) ;
			case "new":
				return new Token(TokenKind.NEW, "new", sposn) ;
			case "public":
				return new Token(TokenKind.VISIBILITY, "public", sposn) ;
			case "private":
				return new Token(TokenKind.VISIBILITY, "private", sposn) ;
			case "static":
				return new Token(TokenKind.ACCESS, "static", sposn) ;
			case "int":
				return new Token(TokenKind.INT, "int", sposn) ;
			case "class":
				return new Token(TokenKind.CLASS, "class", sposn) ;
			case "boolean":
				return new Token(TokenKind.BOOLEAN, "boolean", sposn) ;
			case "else":
				return new Token(TokenKind.ELSE, "else", sposn) ;
			default:
				return new Token(TokenKind.ID, currentSpelling.toString(), sposn);
			}
			
		} else if (currentChar == '"') {
			readChar();
			while (currentChar != '"') {
				currentSpelling.append(currentChar);
				readChar();
			}
			readChar(); // read the second quotes
			return new Token(TokenKind.STRINGLIT, currentSpelling.toString(), sposn);
		} else if (Character.isDigit(currentChar)) {
			//boolean decimal = false;
			while(Character.isDigit(currentChar) /*|| currentChar == '.'*/) {
				/*if (currentChar == '.' && !decimal) {
					decimal = true;
					currentSpelling.append(currentChar);
				} else if (currentChar == '.' && decimal) {
					//no two decimals in numbers
					break;
				}*/
				currentSpelling.append(currentChar);
				readChar();
			}
			return new Token(TokenKind.NUM, currentSpelling.toString(), sposn);
		} else {
			scanError("Unrecognized character '" + currentChar + "' in input");
			return new Token(TokenKind.ERROR, "?", sposn);
		}
	}
	
	private void scanError(String m) {
		reporter.reportError("*** Scan Error:  " + m);
	}
	
	
	private void readChar() {
		try {
			int c;
			if (debug) {
				c = stringReader.read();
			} else {
				c = fileReader.read();
			}
			
			currentChar = (char) c;
			if (c == -1) {
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}

}
