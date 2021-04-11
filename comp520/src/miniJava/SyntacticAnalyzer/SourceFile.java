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
		
		return fail352();
	}
	
	public String lengthCheck() {
		return "class baby{\n"
				+ "static int[] baby2;"
				+ "public static void main(String[] banana){\n"
				+ "int tootle = this.baby2.length;\n"
				+ "int bnn = banana.length;"
				+ "}\n"
				+ "}\n";
	}
	
	public String psvmCheck() {
		return "class baby{\n"
				+ "public static void main(String[] banana){}\n"
				+ "public int baby(){\n"
				+ "return 7;\n"
				+ "}\n"
				+ "}\n";
	}
	
	
	// ----------------------------------- PA 3 --------------------------------------------- //
	
	// >>> PA 3 Fixes <<< //
	/*
	 * - fixed: compiler failed processing fail328.java 
	 * - fixed: fail336, fail342, fail348, fail340, fail351
	 * - fixed: pass321, pass378, pass379, pass382, pass383, pass324
	 * - didn't pass: pass372, pass373, pass374, pass376, pass377, pass380, pass381 
	 */
	
	public String fail352() {
		return "/*** line 9: cannot nonstatic field \"pubfield\" from a static context. \r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class TestClass {\r\n" + 
				"        \r\n" + 
				"    public static void StaticContext() {\r\n" + 
				"\r\n" + 
				"        int x = TestClass.pubfield;\r\n" + 
				"    }\r\n" + 
				"        \r\n" + 
				"    public int pubfield;\r\n" + 
				"}";
	}
	
	public String pass372() {
		return "class TestClass {\r\n" + 
				"        \r\n" + 
				"    public static void staticContext() {\r\n" + 
				"\r\n" + 
				"        TestClass t = null;\r\n" + 
				"        int x = 0;\r\n" + 
				"\r\n" + 
				"        /*\r\n" + 
				"         * VALID\r\n" + 
				"         */\r\n" + 
				"\r\n" + 
				"        // QualifiedRef \r\n" + 
				"        // x = t.pubfield;\r\n" + 
				"        x = t.privfield;\r\n" + 
				"    }\r\n" + 
				"        \r\n" + 
				"       \r\n" + 
				"    public int pubfield;\r\n" + 
				"    private int privfield;\r\n" + 
				"    public static int pubstatfield;\r\n" + 
				"    private static int privstatfield;\r\n" + 
				"}";
	}
	
	public String pass324() {
		return "/**\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class Pass324 {         \r\n" + 
				"    public static void main(String[] args) {\r\n" + 
				"        Pass324 p = new Pass324();\r\n" + 
				"        int x = p.p() + p.x;\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public int x;\r\n" + 
				"    \r\n" + 
				"    public int p() {\r\n" + 
				"        return 3;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass305() {
		return "/**\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class Pass305 { 	\r\n" + 
				"    public static void main(String[] args) {\r\n" + 
				"        System.out.println(3);\r\n" + 
				"    } \r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String fail355() {
		return "/*** line 9: reference does not denote a variable \r\n" + 
				" * COMP 520  \r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class TestClass {\r\n" + 
				"        \r\n" + 
				"    public void nonStaticContext() {\r\n" + 
				"\r\n" + 
				"        int x = OtherClass.opubstatTest.privfn;\r\n" + 
				"    }\r\n" + 
				"        \r\n" + 
				"        \r\n" + 
				"    private int privfn() { return 1; }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"class OtherClass {\r\n" + 
				"        \r\n" + 
				"    public static TestClass opubstatTest;\r\n" + 
				"}";
	}
	
	public String pass321() {
		return "/**\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class Pass321 { 	\r\n" + 
				"    public static void main(String[] args) {\r\n" + 
				"        Pass321 p = new Pass321();\r\n" + 
				"        p.next = p;\r\n" + 
				"        p.next.next.x = 3;\r\n" + 
				"    } \r\n" + 
				"    \r\n" + 
				"    public Pass321 next;\r\n" + 
				"    private int x;\r\n" + 
				"}";
	}
	
	
	public String fail351() {
		return "/*** line 11: reference \"pubfn\" does not denote a field or a variable\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class TestClass {\r\n" + 
				"        \r\n" + 
				"    public static void staticContext() {\r\n" + 
				"\r\n" + 
				"        int x = 0;\r\n" + 
				"\r\n" + 
				"        x = pubfn;\r\n" + 
				"    }\r\n" + 
				"        \r\n" + 
				"        \r\n" + 
				"    public static int pubfn() { return 1; }\r\n" + 
				"}";
	}
	
	public String fail348() {
		return "/*** line 11: cannot reference \"this\" within a static context\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class TestClass {\r\n" + 
				"        \r\n" + 
				"    public static void staticContext() {\r\n" + 
				"        int t = this.statfield;\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public static int statfield; \r\n" + 
				"}        \r\n" + 
				"";
	}
	
	public String fail342() {
		return "/*** line 11: cannot reference \"this\" within a static context\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class TestClass {\r\n" + 
				"        \r\n" + 
				"    public static void staticContext() {\r\n" + 
				"        TestClass t = this;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String fail336() {
		return "/*** line 8: \"foo\" is not a valid qualifier for reference \"c\"\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class Fail328 { 	\r\n" + 
				"    public static void main(String[] args) {\r\n" + 
				"        F05 c = new F05();\r\n" + 
				"        c = c.foo.next;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"class F05 {\r\n" + 
				"    public F05 next;\r\n" + 
				"    public F05 foo() {return this;}\r\n" + 
				"}";
		
	}
	
	public String fail340() {
		// !!!!
		return "/*** line 11: cannot reference \"x\" within the initializing expression of the declaration for \"x\"\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class fail305 { 	\r\n" + 
				"\r\n" + 
				"    int x;\r\n" + 
				"    int y;\r\n" + 
				"\r\n" + 
				"    public void foo() {\r\n" + 
				"	int x = y + x;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String fail328() {
		return "/*** line 10: reference \"d\" of type \"D []\" does not have a public field \"x\"\r\n" + 
				" * COMP 520\r\n" + 
				" * Identification\r\n" + 
				" */\r\n" + 
				"class Fail328 {\r\n" + 
				"\r\n" + 
				"    public D [] d;\r\n" + 
				"\r\n" + 
				"    public void f() {\r\n" + 
				"	int y = d.x;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"class D { public int x; }";
	}
	
	public String bb() {
		return "// PA2 unary and binop precedence\r\n" + 
				"class Unprecedented {\r\n" + 
				"    void p(){\r\n" + 
				"        int x = true ||  new Unprecedented();\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String verStringErr() {
		return "class fold{ \n"
				+ " String bb;\n"
				+ "String[] bb2;\n"
				+ " void main(){\n"
				+ "bb = new String();\n"
				+ "String[] red = new String[10];\n"
				+ "bb2 = null;\n"
				+ "String[] grape = null;\n"
				+ "String varDecl = null;\n"
				+ "}}\n";
	}
	
	public String whileErr() {
		return "class nullTesting{}\n"
				+ "class conditionalTesting{\r\n" + 
				"   boolean baby;\r\n" + 
				"   int pancake(boolean smell){\r\n" + 
				"      if (smell) {\r\n" + 
				"         nullTesting bb = null;\r\n" + 
				"      } else {\r\n" + 
				"         nullTesting bb = new nullTesting();\r\n" + 
				"      }\r\n" + 
				"\r\n" + 
				"      while (new nullTesting() != null) \r\n" + 
				"      {\r\n" + 
				"         conditionalTesting tt = this;\r\n" + 
				"      }\r\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String thisCall() {
		return "class Test { \n"
				+ " public void main(){ \n"
				+ "this();\n}"
				+ "}";
	}
	
	public String stringNull() {
		return " class Baby { static String toodle;} \n"
				+ "class Main { \n"
				+ "static String toodle;\n"
				+ "void goober() { \n"
				+ "String baby = Baby.toodle;\n"
				+ "}\n"
				+ "}\n";
	}
	
	public String pass140() {
		return "// PA1 parse field decl pass\r\n" + 
				"class id {\r\n" + 
				"    public static Type x;\r\n" + 
				"}\r\n" + 
				"";
	}
	
	/* I have tested:
	 illegal characters, multi line and single line comments
	ids with numbers and underscores in them, long numbers
	* 
	*/
	
	/*
	 * PA3 testing with PA1 tests:
	 * - pass121 should fail
	 * - pass123 should fail
	 * - pass124 should fail
	 * - pass126 should fail - verify that pass126v2 is valid
	 * - pass127 should fail
	 * - pass128 should fail
	 * - pass129 should fail
	 * 140 - 143
	 * - pass151 should fail
	 * - pass152 should fail
	 * - pass153 should fail
	 * - pass154 should fail
	 * - pass156 verify if variables have to be declared sequentially
	 * - pass157 should fail
	 * - pass158 should fail BUT review and fix (verify)
	 * - pass159 should fail
	 * - pass160 should fail
	 * - pass161 review and fix (verify)
	 * - pass163 should fail
	 * - pass164 should fail
	 * - pass165 verify if a void method returns a null value or if there's an issue
	 * - pass166 should fail (verify if this = new Class() is valid or invalid)
	 * - pass167 should fail 
	 * - pass169 should fail
	 * - pass170 should fail BUT review and fix
	 */
	
	/* PA3 testing with PA1 tests:
	 * - pass121 should fail
	 * - pass123 should fail
	 * - pass124 should fail
	 * - pass126 should fail - verify that pass126v2 is valid
	 * - pass127 should fail
	 * - pass128 should fail
	 * - pass129 should fail
	 * - pass151 should fail
	 * - pass152 should fail
	 * - pass153 should fail
	 * - pass154 should fail
	 * - pass156 verify if variables have to be declared sequentially
	 * - pass157 should fail
	 * - pass158 should fail BUT review and fix (verify)
	 * - pass159 should fail
	 * - pass160 should fail
	 * - pass161 review and fix (verify)
	 * - pass163 should fail
	 * - pass164 should fail
	 * - pass165 verify if a void method returns a null value or if there's an issue
	 * - pass166 should fail (verify if this = new Class() is valid or invalid)
	 * - pass167 should fail 
	 * - pass169 should fail
	 * - pass170 should fail BUT review and fix
	 */
	
	/*PA3 testing w PA2 tests:
	 * pass155: 
	 */
	
	public String pass145() {
		return "// PA1 parse field decl pass\r\n" + 
				"class id {\r\n" + 
				"    int [] x;\r\n" + 
				"    Foo [] y;\r\n" + 
				"}";
	}
	
	public String secondPa3Test() {
		return "class yellow { \n"
				+ "public static void main (String[] args){ \n"
				+ " System.out.println(42); \n"
				+ " String baby = args[0];\n"
				+ " yellow worm = new yellow();\n"
				+ "}"
				+ "}";
	}
	
	public String testString() {
		return "// PA1 parse parse pass\r\n" + 
				"class Baby{\r\n" + 
				"    static int angsty;\r\n" + 
				"    static void main(String[] args){\r\n" +
				"        String[] noodle = new String[10];\r\n" + 
				"        String baby = new String();\r\n" + 
				"        //boolean woobly = noodle != baby;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	
	
	// Test cases: 
	/*//gets the error "non-static method marachi() cannot be referenced from a static context
	 * public class HelloWorld{
    static int baby = 1;
    int marachi(){
       return this.baby; 
    }
     public static void main(String []args){
        System.out.println(marachi());
     }
}
	 */
	public String testy() {
		// good job!! same error
		return "class HelloWorld{\r\n" + 
				"    static int baby;\r\n" + 
				"    int marachi(){\r\n" + 
				"       return this.baby; \r\n" + 
				"    }\r\n" + 
				"     public static void main(String []args){\r\n" + 
				"        System.out.println(marachi());\r\n" + 
				"     }\r\n" + 
				"}";
	}
	
	public String pass169v2() {
		return "// PA1 parse new pass\r\n"
				+ "class Foo{}\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"  static Foo food;\n  "
				+ "void p(int baby) {\r\n" + 
				"	Foo [] foo = new Foo [10];\r\n"
				+ "foo[86 + -52 -- baby] = Test.food;\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass169() {
		return "// PA1 parse new pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p() {\r\n" + 
				"	Foo [] foo = new Foo [10];\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	
	public String pass164v2() {
		return "// PA1 parse assign pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" +
				"int[] Test;\n" +
				"    void p(int a) {\r\n" + 
				"        Test [ a + 1]  = a * 3;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass164() {
		return "// PA1 parse assign pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p(int a) {\r\n" + 
				"        Test [ a + 1]  = a * 3;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass163v2() {
		return "// PA1 parse decl pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p(Test[] a) {\r\n" + 
				"        Test [ ]  v = a;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass163() {
		return "// PA1 parse decl pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p(int a) {\r\n" + 
				"        Test [ ]  v = a;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	
	public String pass160v2() {
		return "// PA1 parse local decls pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"static A monkey;\n" +
				"    void p(boolean c) {\r\n" + 
				"        A a = Test.monkey;\r\n" + 
				"	boolean b = c;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass160() {
		return "// PA1 parse local decls pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p() {\r\n" + 
				"        A a = 23;\r\n" + 
				"	boolean b = c;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass159v2() {
		// this should pass but it does not :(
		return "// PA1 parse refs pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    int [] a;\r\n" + 
				"    Test [] t;\r\n" + 
				"\r\n" + 
				"    int p(int e) {\r\n" + 
				"        t[e] = this;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass159() {
		return "// PA1 parse refs pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    int [] a;\r\n" + 
				"    Test [] t;\r\n" + 
				"\r\n" + 
				"    int p() {\r\n" + 
				"        t[e] = this + 1;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass157() {
		return "// PA1 parse method pass\r\n" + 
				"class Test {\r\n" + 
				"void foo () {\r\n" + 
				"        if (x != 0)\r\n" + 
				"	    return x;\r\n" + 
				"        return y; \r\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String pass157v2() {
		return "// PA1 parse method pass\r\n" + 
				"class Test {\r\n" + 
				"  static int x;  "
				+ "int y;"
				+ "int foo () {\r\n" + 
				"        if (x != 0)\r\n" + 
				"	    return x;\r\n" + 
				"        return y; \r\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String pass156() {
		return "// PA1 lex binop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"        int x = 1 + 2;\r\n" + 
				"	int y = x * 3;\r\n" + 
				"	int z = y / 4;\r\n" + 
				"	boolean a = z > 5;\r\n" + 
				"	boolean b = y >= 6;\r\n" + 
				"	boolean c = x < 7;\r\n" + 
				"	int v = x <= 8;\r\n" + 
				"	int w = d != 9;\r\n" + 
				"	boolean d = a && false ;\r\n" + 
				"	boolean r = d || true;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	
	public String pass154() {
		return "// PA1 parse methods pass\r\n" + 
				"class MainClass {\r\n" + 
				"   public static void main (String [] args) {\r\n" + 
				"   }\r\n" + 
				"}\r\n" + 
				"class SuperClass\r\n" + 
				"{\r\n" + 
				"   public void setWorth (int worth){\r\n" + 
				"      integer = worth;\r\n" + 
				"   }\r\n" + 
				"   \r\n" + 
				"   public int getWorth (){\r\n" + 
				"      return this.integer;\r\n" + 
				"   }\r\n" + 
				"   \r\n" + 
				"   public void setTruth (boolean truth){\r\n" + 
				"      bool = truth;\r\n" + 
				"   }\r\n" + 
				"   \r\n" + 
				"   public int getTruth (){\r\n" + 
				"      return this.bool;\r\n" + 
				"   }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass154v2() {
		return "// PA1 parse methods pass\r\n" + 
				"class MainClass {\r\n" + 
				"   public static void main (String [] args) {\r\n" + 
				"   }\r\n" + 
				"}\r\n" + 
				"class SuperClass\r\n" + 
				"{\r\n" + 
				" static int integer;\n" +
				" static int bool;\n" +
				"   public void setWorth (int worth){\r\n" + 
				"      integer = worth;\r\n" + 
				"   }\r\n" + 
				"   \r\n" + 
				"   public int getWorth (){\r\n" + 
				"      return this.integer;\r\n" + 
				"   }\r\n" + 
				"   \r\n" + 
				"   public void setTruth (boolean truth, boolean bool){\r\n" + 
				"      bool = truth;\r\n" + 
				"   }\r\n" + 
				"   \r\n" + 
				"   public int getTruth (){\r\n" + 
				"      return this.bool;\r\n" + 
				"   }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass153v2() {
		return "// PA1 parse new pass\r\n" + 
				"class Foo {\r\n" + 
				"   void bar() {\r\n" + 
				"      int [] arr = new int[20];\r\n" + 
				"      Foo [] foo = new Foo[30];\r\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String pass153() {
		return "// PA1 parse new pass\r\n" + 
				"class Foo {\r\n" + 
				"   void bar() {\r\n" + 
				"      int [] arr = new int[20] + 1;\r\n" + 
				"      Foo [] foo = 2 + new Foo[30];\r\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String pass152() {
		return "// PA1 parse new pass\r\n" + 
				"class MainClass {\r\n" + 
				"   public static void main (String [] args) {\r\n" + 
				"      SecondSubClass newobj = new SecondSubClass ();\r\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String pass152v2() {
		return "// PA1 parse new pass\r\n" +
				"class SecondSubClass{ThirdSubClass potato;}\n" +
				"class ThirdSubClass{}\n"
				+ "class MainClass {\r\n" + 
				"  static ThirdSubClass ab;\n "
				+ "public static void main (String [] args) {\r\n"
				+ "int banana = 52;\n" + 
				"      SecondSubClass newobj = new SecondSubClass();\r\n"
				+ "ab = newobj.potato;\n" + 
				"   }\r\n" + 
				"}";
	}
	
	public String pass151v2() {
		return "// PA1 parse identifiers pass\r\n" + 
				" class B{ A potato;} \n"
				+ "class A {}\n"
				+ "class Keywords {\r\n" + 
				"\r\n" + 
				"    // minijava keywords are lower case only\r\n" + 
				"    A For;\n"
				+ "B FOR;\n" +
				"    void p(int while_1, int New, A Class, B RETURN) {\r\n" + 
				"        int format = while_1;\r\n" + 
				"        int Int = New;\r\n" + 
				"        For = Class;     \r\n" + 
				"        FOR = RETURN;\r\n"
				+ "		 For = RETURN.potato;\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"   public int declare () {\r\n" + 
				"      boolean iF = true; \r\n" + 
				"      boolean Then = false; \r\n" + 
				"      boolean else1 = false;\r\n" + 
				"\r\n" + 
				"      if (true == false) { else1 = iF == Then; }\r\n" + 
				"   }\r\n" + 
				"} \r\n"; 
	}
	
	public String pass151() {
		return "// PA1 parse identifiers pass\r\n" + 
				"class Keywords {\r\n" + 
				"\r\n" + 
				"    // minijava keywords are lower case only\r\n" + 
				"    void p() {\r\n" + 
				"        int format = while_1;\r\n" + 
				"        int Int = New;\r\n" + 
				"        For = Class;     \r\n" + 
				"        FOR = RETURN;\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"   public int declare () {\r\n" + 
				"      boolean iF = true; \r\n" + 
				"      boolean Then = false; \r\n" + 
				"      boolean else1 = false;\r\n" + 
				"\r\n" + 
				"      if (true == false) { else1 = iF == Then; }\r\n" + 
				"   }\r\n" + 
				"} \r\n"; 
	}
	
	public String pass129v2() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"		 int b = 65 * 6 - 4 + 72 / ---8 * 73653;\n" +
				"        int x =  b - - - -b;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	public String pass129() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"        int x =  b - - - -b;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass127v2() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(int boogy){\r\n" + 
				"        int y = --boogy;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass127() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"        int y = --y;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass126v2() {
		return "// PA1 lex binop pass\r\n" + 
				"class id {\r\n" + 
				"	 boolean x;\n" +
				"    void p(){\r\n" + 
				"        boolean x = true && false || x;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass126() {
		return "// PA1 lex binop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"        boolean x = true && false || x;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass124v2() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"	 int b;\n" + 
				"    void p(){\r\n" + 
				"        boolean x =  10 >- b;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"";
	}
	public String pass124() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"        boolean x =  10 >- b;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"";
	}
	
	public String pass123v2() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"		 boolean b = true;\n" +
				"        boolean x =  !!!!!b;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"";
	}
	public String pass123() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" + 
				"        boolean x =  !!!!!b;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"";
	}
	
	public String pass170v2() {
		return "// PA1 parse refs decls pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"  static void that(int a) { }\n  "
				+ "int p(Test that) {\r\n" + 
				"        this = that;\r\n" + 
				"        this();\r\n" + 
				"        this.that(5);\r\n" + 
				"	this.that[2] = 3;\r\n" + 
				"        this.that.those[3]= them;\r\n" + 
				"        this.that.those();\r\n" + 
				"        int [] x = 1;\r\n" + 
				"        a b = c;\r\n" + 
				"	p();\r\n" + 
				"	p.b[4] = 5;\r\n" + 
				"	p.b(3);\r\n" + 
				"	int z = this.p(x) * that.q() + those.r[a.p];\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass170() {
		return "// PA1 parse refs decls pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    int p() {\r\n" + 
				"        this = that;\r\n" + 
				"        this();\r\n" + 
				"        this.that(5);\r\n" + 
				"	this.that[2] = 3;\r\n" + 
				"        this.that.those[3]= them;\r\n" + 
				"        this.that.those();\r\n" + 
				"        int [] x = 1;\r\n" + 
				"        a b = c;\r\n" + 
				"	p();\r\n" + 
				"	p.b[4] = 5;\r\n" + 
				"	p.b(3);\r\n" + 
				"	int z = this.p(x) * that.q() + those.r[a.p];\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass167() {
		return "// PA1 parse assign pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p() {\r\n" + 
				"	x.y = z;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass167v2() {
		return "// PA1 parse assign pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n"
				+ "Test x;\n"
				+ "int y;" + 
				"    void p() {\r\n" + 
				"	x.y = 76 / 54;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass166v2() {
		return "// PA1 parse assign pass\r\n"
				+ "class Foo{}" + 
				"class Test {\r\n" + 
				"\r\n" + 
				" int v;\n"+
				"    void p(Test a) {\r\n"
				+ "this = new Test();\n" + 
				"	this.p(new Test());\r\n" + 
				"	a.v = 4;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass166() {
		return "// PA1 parse assign pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p(int a) {\r\n" + 
				"	this.p(2,3);\r\n" + 
				"	a.v = 4;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass165v2() {
		return "// PA1 parse call pass\r\n"  
				+ "class C {\r\n" + 
				"\r\n" + 
				"    void p(int a) {\r\n" + 
				"	C c = new C();\r\n" + 
				"	int y = p();\r\n" + 
				"	int x = c.p(2,3) ;\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass165() {
		return "// PA1 parse call pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p(int a) {\r\n" + 
				"	C c = new C();\r\n" + 
				"	y = p();\r\n" + 
				"	x = c.p(2,3) + x[2];\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass162() {
		return "// PA1 parse ref pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p(int a, boolean b) {\r\n" + 
				"        this.p(a,b);\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass161() {
		// are classes indexable? is this an identification or a type checking error? (currently type error)
		return "class Test {\r\n" + 
				"\r\n" + 
				"    int p() {\r\n" + 
				"	this[3] = 4;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	// TODO: review this: if local variable hides member,
	// if the types don't match, do we go find that higher member type??
	public String pass158v2() {
		return "// PA1 parse refs pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"  int[] a;  "
				+ "void p() {\r\n"
				+ "	int b = 89 * 2 / 3;\n	 "
				+ "	boolean a;\n" + 
				"        a = true;\r\n" + 
				"        a [b] = c;\r\n" + 
				"        p ();\r\n" + 
				"        a.b = d;\r\n" + 
				"        c.p(e);\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass158() {
		return "// PA1 parse refs pass\r\n" + 
				"class Test {\r\n" + 
				"\r\n" + 
				"    void p() {\r\n" + 
				"        a = true;\r\n" + 
				"        a [b] = c;\r\n" + 
				"        p ();\r\n" + 
				"        a.b = d;\r\n" + 
				"        c.p(e);\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
	}
	
	public String pass146() {
		return "// PA1 parse return pass\r\n" + 
				"class NonTokens{\r\n" + 
				"   void main () {\r\n" + 
				"      return;\r\n" + 
				"   }\r\n" + 
				"}\r\n" + 
				"";
	}

	public String pass121v2() {
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" +
				"		 int b = 56 / 72;\n" +
				"        int x =  - b;\r\n" + 
				"        boolean y = !false;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String pass121() {
		// should fail on line 6 bc can't use variable itself in declaration
		// changed so b would be declared
		return "// PA1 lex unop pass\r\n" + 
				"class id {\r\n" + 
				"    void p(){\r\n" +
				"		 int b = 56 / 72;\n" +
				"        int x =  - b;\r\n" + 
				"        boolean y = !y;\r\n" + 
				"    }\r\n" + 
				"}";
	}
	
	public String fail101() {
		return "// PA1 lex id fail\r\n" + 
				"class _id {}\n";
	}
	
	public String tc3() {
		return " class Test { \n"
				+ "int[] banana; \n"
				+ "baby wow; \n"
				+ "public void go() { \n"
				+ "banana[ true || 55 + wow.sweet.monkey] = false || true && 7; \n"
				+ "int margarita = -true;\n "
				+ "}\n"
				+ "}\n"
				+ "class baby {\n"
				+ "foo sweet;\n"
				+ "}"
				+ "class foo {\n"
				+ "int monkey;\n"
				+ "}";
	}
	
	// TODO: how many errors for this? 2 or 3?
	// do we rely on the expression or the operator for the returned type?
	public String tc2() {
		return " class Test { \n"
				+ "boolean banana;"
				+ "public void go() { \n"
				+ "banana = false || true && 7; \n"
				+ "int margarita = -true;\n "
				+ "}\n"
				+ "}"
				+ "class baby {}"
				+ "class foo {}";
	}
	
	public String tc1() {
		return " class Test { \n"
				+ "baby[] banana;"
				+ "public void go() { \n"
				+ "banana[0] = new baby(); \n "
				+ "}\n"
				+ "}"
				+ "class baby {}"
				+ "class foo {}";
	}
	
	
	public String staticMethodCheck() {
		return " class Test { \n"
				+ "static int banana; \n"
				+ "public static void go() { \n"
				+ "p.x = new Foo(); \n"
				+ "banana = 4 / 2; \n "
				+ "}\n"
				+ "public static Foo p; "
				+ "}"
				+ "class Foo { public int x; }";
	}
	
	public String conditionCheck() {
		return " class Test { \n"
				+ "int banana; \n"
				+ "public void go() { \n"
				+ "if(true)\n"
				+ "banana = 2; }\n"
				+ "public Foo p; }"
				+ "class Foo { public int x; }";
	}
	
	public String babyPa3Test() {
		// error line 5
		return " class Test { \n public int go() { \n Foo p = new Foo();\n this.p = p; \n return p.x; }\n public Foo p; } \n class Foo { public int x; } \n";
	}
	
	public String goodBabyPa3Test() {
		return "class Test { \n" 
				+ "public int go() { \n"
				+ "Foo p = new Foo();\n"
				+ "this.p = p; \n"
				+ "this.p.x = 2; \n"
				+ "return p.x; }\n"
				+ "public Foo p; }"
				+ "class Foo { public int x; }";
	}
	
	public String staticPa3Test() {
		return " class Test { \n" 
				+ "public int go(Foo p) { \n"
				+ "return p.x; }\n"
				+ "} \n"
				+ "class Foo { static int x; }\n";
	}
	
	public String badBabyPa3Test() {
		return " class Test { \n" 
				+ "public int go() { \n"
				+ "Foo p = new Foo();\n"
				+ "this.p = p; \n"
				+ "this.p.y = 2; \n"
				+ "return p.x; }\n"
				+ "public Foo p; }"
				+ "class Foo { public int x; }";
	}
	
	public String classPa3Test() {
		return " class Foo { } \n"
				+ " class Foo {} ";
	}
	
	public String memberPa3Test() {
		return " class Foo { static int x; "
				+ " void x() {} } \n";
	}
	
	public String paramPa3Test() {
		return " class Foo { static int y; "
				+ " void x(int y, int[] y) {} } \n";
	}
	
	public String firstPa3Test() {
		return "class yellow { \n"
				+ "public static void main (String[] args){ \n"
				+ " System.out.println(42); \n"
				+ "}"
				+ "}";
	}
	
	public String badPa3Test() {
		return "class yellow { \n"
				+ "public static void main (String[] args){ \n"
				+ " System.out.baby(42); \n"
				+ "}"
				+ "}";
	}
	
	public String pass128v2() {
		return "// PA1 lex unop pass \n"
		+ "class id { \n"
		+ "	  int b;\n"	
		+ "   void p(){ \n"
		+ "       int x =  -b - - b; \n"
		+    "} \n"
		+ "} \n";
	}
	
	public String pass128() {
		return "// PA1 lex unop pass \n"
		+ "class id { \n"
		+ "   void p(){ \n"
		+ "       int x =  -b - - b; \n"
		+    "} \n"
		+ "} \n";
	}
	
	public String nullTest() {
		return "// PA1 lex unop pass \n"
		+ "class id { \n"
		+ "   void p(){ \n"
		+ "       int x =  null; \n"
		+    "} \n"
		+ "} \n";
	}
	
	public String badNullTest() {
		return "// PA1 lex unop pass \n"
		+ "class id { \n"
		+ "   void p(){ \n"
		+ "       int null = 45; \n"
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
