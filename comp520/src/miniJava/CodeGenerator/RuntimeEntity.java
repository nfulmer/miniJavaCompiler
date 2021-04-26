package miniJava.CodeGenerator;

import mJAM.Machine.Reg;

public class RuntimeEntity {
	
	// added for pa4
	
	public int size;
	public Reg reg;
	public int offset;

	public RuntimeEntity(int size, Reg reg, int offset) {
		this.size = size;
		this.reg = reg;
		this.offset = offset;
		
	}

}
