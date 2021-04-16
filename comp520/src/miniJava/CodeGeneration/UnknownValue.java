package miniJava.CodeGeneration;

public class UnknownValue extends RuntimeEntity {
	
	// public Address address; --> from ppt 14 CodeGeneration slide 6
	public int address;

	public UnknownValue(int size, int address) {
		super(size);
		this.address = address;
	}

}
