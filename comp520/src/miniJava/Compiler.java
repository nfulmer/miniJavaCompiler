package miniJava;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.CodeGeneration.REDCreate;
import miniJava.ContextualAnalysis.*;
import miniJava.SyntacticAnalyzer.*;

public class Compiler {

	public Compiler() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String args[]) {
		InputStream inputstream = null;
		Compiler c = new Compiler();
		SourceFile sf;
		boolean debug;
		int rc;
		if (args.length == 0) {
			debug = true;
			sf = new SourceFile("text_test.javac");
		} else {
			debug = false;
			sf = new SourceFile(args[0]);
			try {
				inputstream = new FileInputStream(args[0]);
			} catch (FileNotFoundException e) {
				rc = 1;
				if (debug) {
					System.out.println("Exit code: " + rc);
				}
				e.printStackTrace();
				System.exit(rc);
			}
		}
		
		if (debug) {
			rc = c.compileProgram(sf.test(), debug);
		} else {
			rc = c.compileProgram(args[0], debug);
		}
		if (debug) {
			System.out.println("Exit code: " + rc);
		}
		System.exit(rc);
	}
	
	int compileProgram(String sourceFile, boolean debug) {
		ErrorReporter er = new ErrorReporter();
		Scanner scan = new Scanner(er, sourceFile, debug);
		Parser ps = new Parser(scan, er, debug);
		
		AST tree = ps.parse();
		
		if (er.numErrors() > 0) {
			if (debug) {
				System.out.println("NOT VALID PROGRAM");
			}
			return 4;
		} else {

			Identification id = new Identification(tree, er);
			// PA3 specifications: it's reasonable to stop contextual analysis upon failure in identification 
			// because further checking might be meaningless
			if (er.numErrors() > 0) {
				if (debug) {
					System.out.println("NOT VALID PROGRAM");
				}
				return 4;
			} else {
				TypeChecking tc = new TypeChecking(tree, er);
				
				if (er.numErrors() > 0) {
					if (debug) {
						System.out.println("NOT VALID PROGRAM");
					}
					return 4;
				} else {
					
					//REDCreate cGen = new REDCreate(tree, er);
					
					if (er.numErrors() > 0) {
						if (debug) {
							System.out.println("NOT VALID PROGRAM");
						}
						return 4;
					} else {
						if (debug) {
							System.out.println("VALID PROGRAM");
						}
						return 0;
					}
				}
			}
			
			
		}
		
		
	}
}

