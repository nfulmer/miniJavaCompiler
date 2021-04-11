package tester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MyTest4 {
	
	private static String projDir;
	private static File classPath;
	private static File testDir;

    public static void main(String[] args) throws IOException, InterruptedException {

		// project directory for miniJava and tester
		projDir = System.getProperty("user.dir");
		System.out.println("Run pa3_tests on miniJava compiler in " + projDir);
		
		// compensate for project organization 
		classPath = new File(projDir + "/bin");
		if (!classPath.isDirectory()) {
			// no bin directory in project, assume projDir is root for class files
			classPath = new File(projDir);
		}

		// miniJava compiler mainclass present ?
		if (! new File(classPath + "/miniJava/Compiler.class").exists()) {
			System.out.println("No miniJava Compiler.class found (has it been compiled?) - exiting");
			return;
		}

		// test directory present ?
		testDir = (new File(projDir + "/tests").getCanonicalFile());
		if (! testDir.isDirectory()) {
			System.out.println("test directory not found - exiting!");
			return;
		}

		System.out.println("Running tests from directory " + testDir);
        int failures = 0;
        for (File x : testDir.listFiles()) {
        	if (x.getName().startsWith("pa")) {
        		continue;
        	}
        	int returnCode = runTest(x); 
        	if (returnCode == 1) {
				System.err.println("### miniJava Compiler fails while processing test " + x.getName());
				failures++;
				continue;
			}
        }
    }
	
	 private static int runTest(File x) throws IOException, InterruptedException {
	        String testPath = x.getPath();
	        ProcessBuilder pb = new ProcessBuilder("java", "miniJava.Compiler", testPath);
	        pb.directory(classPath);
	        pb.redirectErrorStream(true);
	        Process p = pb.start();

	        processStream(p.getInputStream());
	        if (!p.waitFor(5, TimeUnit.SECONDS)) {
				// hung test
				p.destroy();
				return 130;  // interrupted
			}
	        return p.exitValue();
	    }
	 
	 public static void processStream(InputStream stream) {
	        Scanner scan = new Scanner(stream);
	        while (scan.hasNextLine()) {
	            String line = scan.nextLine();
	            if (line.startsWith("*** "))
	                System.out.println(line);
	            if (line.startsWith("ERROR")) {
	                System.out.println(line);
	            }
	        }
	        scan.close();
	    }

}
