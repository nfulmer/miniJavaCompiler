package tester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* Automated regression tester for Checkpoint 1 tests
 * Created by Max Beckman-Harned and Jan Prins 
 * To install and run the tester see the instructions on the COMP 520 website.
 */
public class Checkpoint1 {
	
	static ExecutorService threadPool = Executors.newCachedThreadPool();

	public static void main(String[] args) throws IOException, InterruptedException {
		
		// project directory for miniJava and tester
		String projDir = System.getProperty("user.dir");
		System.out.println("Run pa1_tests on miniJava compiler in " + projDir);
		
		// compensate for project organization 
		File classPath = new File(projDir + "/bin");
		if (!classPath.isDirectory()) {
			// no bin directory in project, assume projDir is root for class files
			classPath = new File(projDir);
		}
		
		// miniJava compiler mainclass present ?
		if (! new File(classPath + "/miniJava/Compiler.class").exists()) {
			System.out.println("No miniJava compiler found - exiting!");
			return;
		}
		
		System.out.println(projDir);
		// test directory present ?
		File testDir = new File(projDir + "/../tests/pa1_tests");
		if (! testDir.isDirectory()) {
			System.out.println("pa1_tests directory not found - exiting!");
			return;
		}
		
		// run tests
		int failures = 0;
		for (File x : testDir.listFiles()) {
			int returnCode = runTest(x, classPath);
			if (returnCode == 1) {
				System.err.println("### miniJava Compiler failed while processing test " + x.getName());
				failures++;
				continue;
			}
			if (returnCode == 130) {
				System.err.println("### miniJava Compiler hangs on test " + x.getName());
				failures++;
				continue;
			}
			if (x.getName().indexOf("pass") != -1) {
				if (returnCode == 0)
					System.out.println(x.getName() + " passed successfully!");
				else if (returnCode == 4) {
					failures++;	
					System.err.println(x.getName()	+ " failed but should have passed!");
				}
			} else {
				if (returnCode == 4)
					System.out.println(x.getName() + " failed successfully!");
				else {
					System.err.println(x.getName() + " did not fail properly!");
					failures++;
				}
			}
		}
		System.out.println(failures + " failures in all.");	
	}
	
	private static int runTest(File x, File cp) throws IOException, InterruptedException {
		String testPath = x.getPath();
		ProcessBuilder pb = new ProcessBuilder("java", "miniJava.Compiler", testPath);
		pb.directory(cp);
		Process p = pb.start();
		threadPool.execute(new ProcessOutputter(p.getInputStream(), false));
		if (!p.waitFor(4, TimeUnit.SECONDS)) {
			// hung test
			p.destroy();
			return 130;  // interrupted
		}
		return p.exitValue();
	}
	
	static class ProcessOutputter implements Runnable {
		private Scanner processOutput;
		private boolean output;
		
		public ProcessOutputter(InputStream _processStream, boolean _output) {
			processOutput = new Scanner(_processStream);
			output = _output;
		}
		@Override
		public void run() {
			while(processOutput.hasNextLine()) {
				String line = processOutput.nextLine();
				if (output)
					System.out.println(line);
			}
		}
		
		
	}
}

