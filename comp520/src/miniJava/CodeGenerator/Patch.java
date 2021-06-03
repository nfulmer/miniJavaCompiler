package miniJava.CodeGenerator;

import miniJava.AbstractSyntaxTrees.MethodDecl;

public class Patch {
	MethodDecl md;
	int address;

	public Patch(MethodDecl md, int address) {
		this.md = md;
		this.address = address;
	}

}
