package miniJava.SyntacticAnalyzer;

import java.io.File;
import java.nio.file.*;

public class SourceFile {

	File file;
	public SourceFile(String fileName) {
		file = new File(Paths.get("").toString() + "/" + fileName);
	}
	
	public String test() {
		String t;
		System.out.println("TESTING - DEBUG MODE");
		//t = "class Pancake { int pan ; }"; //--> valid
		//t = "class Pancake { public static int pan ; }"; //--> valid
		//t = "class Pancake { public static pan ; }"; // not valid
		//t = "class Pancake { public static pan  }$"; // not valid
		return noBaby();
	}
	
	/* I have tested:
	 illegal characters, multi line and single line comments
	ids with numbers and underscores in them, long numbers
	* 
	*/
	
	public String pass128() {
		return "// PA1 lex unop pass \n"
		+ "class id { \n"
		+ "   void p(){ \n"
		+ "       int x =  -b - - b; \n"
		+    "} \n"
		+ "} \n";
	}
	
	public String impor() {
		return "// PA1 lex unop pass \n"
		+ "class banana { \n"
		+ "   void p(){ \n"
		+ "       // int x =  b + b / b || b >= b - - - b ; \n"
		+ "          int y = x * ---y / !-h * j;"
		+    "} \n"
		+ "} \n";
	}
	
	public String pass119() {
		return "// PA1 lex comment pass \n"
				+ "class /*/**/ id {}";
	}
	
	public String pa2Sample() {
		return "// simple PA2 example \n"
				+ "class PA2sample{ \n"
				+ "\n"
				+ "public boolean c; \n"
				+ "public static void main(String[] args){\n"
				+ "if (true) \n"
				+ "this.b[3] = 1 || this.cat + 2 - 4 * xx && -!-!x; \n"
				+ "}\n"
				+ "}\n";
	}
	
	public String baby() {
		//valid
		return " class yankee_doo11de { "
				+ "private int yY;"
				+ "static boolean u8_ () {{int[] CAP_SAP = - 8756239;}}  "
				+ "}"
				+ " class t76__ {"
				+ " public static void gerge87 (frank[] yeezy){"
				+ " return;"
				+ "} "
				+ "}";
	}
	
	public String noBaby2() {
		//valid
		return " class yankee_doo11de { "
				+ "private int yY;"
				+ "static boolean u8_ () {{int[] CAP_SAP = scooby + - ( t + gerbils(45, true, baby)) || fiend && maryanne >= --- yeedle39 / ug == tom;}}  "
				+ "}"
				+ " class t76__ {"
				+ " public static void gerge87 (frank[] yeezy){"
				+ " return;"
				+ "} "
				+ "}";
	}
	
	public String baby2() {
		//valid
		return " class yankee_doo11de { "
				+ "private int yY;"
				+ "static boolean u8_ () {{dogtoe23.bark.yelp = new george();}}  "
				+ "}"
				+ "// wowowowow i love baked *** beans!!! \n"
				+ " class t76__ {"
				+ " public static void gerge87 (frank[] yeezy){"
				+ " return;"
				+ "} "
				+ "}";
	}
	
	public String baby3() {
		//valid
		return " class yankee_doo11de { "
				+ "private int yY;"
				+ "static boolean u8_ () {{dogtoe23.bark.yelp = new george();}}  "
				+ "}"
				+ "/* wowowowow i love baked *** beans!!! \n *yeezus bojeesus //// wowow */"
				+ " class t76__ {"
				+ " public static void gerge87 (frank[] yeezy){"
				+ " return;"
				+ "} "
				+ "}";
	}
	
	public String baby1() {
		//valid
		return " class yankee_doo11de { "
				+ "private int yY;"
				+ "static boolean u8_ () {{dogtoe23.bark.yelp = new george();}}  "
				+ "}"
				+ " class t76__ {"
				+ " public static void gerge87 (frank[] yeezy){"
				+ " return;"
				+ "} "
				+ "}";
	}
	
	private String noBaby() {
		// not valid
		//return "class yan^kee_doodle { private static int[] mango;}";
		//return "class yankee_d00dle { static private boolean rose;}";
		//return "class _yankee_d00dle { static private boolean rose;}";
		return "class yankee_d00dle { private static int 8yeedle;}";

	}
	
	private String ughh() {
		return "class Listicle { public ListItem[] makeMeAList(int length) {"
		+ "ListItem[] list = new ListItem[length];"
		+ "int i = 0;"
		+ "while (i < length) {"
		+ "ListItem new_item = new ListItem();"
		+ "new_item.setValue(Rando.getRandomInt()); \n"
		+ "list[i] = new_item;"
		+ "i = i + 1;"
		+ "}"
		+ "return list;"
		+ "}"
		+ "}";
	}
	
	//TO-DO: generate examples of a valid minijava program:
	//ex: "class Pancake { public static int pan; } 

}
