package miniJava.CodeGeneration;

public class KnownValue extends RuntimeEntity {
	
	// added for pa4
	
	public int value; // the known value

	public KnownValue(int size, int value) {
		super(size);
		this.value = value;
	}

}
