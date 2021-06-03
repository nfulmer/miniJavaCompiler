package miniJava.SyntacticAnalyzer;

public class SourcePosition {
	
	int position;

	public SourcePosition(int position) {
		// TODO Auto-generated constructor stub
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void advancePosition() {
		position++;
	}
	public void advancePosition(int i) {
		position += i;
	}
}
