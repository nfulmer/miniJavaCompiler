package miniJava.ContextualAnalysis;

import java.util.HashMap;

import miniJava.AbstractSyntaxTrees.*;

public class IdentificationTable extends HashMap<String, Declaration> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IdentificationTable() {
		super();
	}
	
	/*Java: class HashMap<String,Decl> clear(), boolean containsKey( String id ), Decl put( String id , Decl decl ) // associate id with decl
	 * Decl get( String id ) // decl or null, if id not in hashmap
	 * void remove( String id ) // remove current association of id, if any */
	
	void openScope(){
	
	}
	
	void closeScope() {
		
	}
	
	Declaration Get(String name){
		// Get(id.spelling()) returns innermost declaration
		
		return this.get(name);
	}
	
	/* Implementation challenges
	 * remove mappings when leaving scope
	 * handling multiple declarations */

}
