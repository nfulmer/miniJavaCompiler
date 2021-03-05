package miniJava.SyntacticAnalyzer;

public class SourcePosition {
	
	int position;

	public SourcePosition() {
		// TODO Auto-generated constructor stub
		position = 0;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void advancePosition() {
		position = position + 1;
	}
}
