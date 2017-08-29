package test;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.junit.Test;

import ch.trick17.javaprocesses.JavaProcessBuilder;
import rolez.checked.transformer.MainDriver;

public class MainDriverTests {
	
	@Test
	public void testMainDriver() {
		runPipeline("testMainDriver");
	}
	
	// TODO: Make this method more generic, that it can be used by different test methods
	private void runPipeline(String methodName) {

		// Delete output folder and output file
		deleteRecursive("sootOutput/" + methodName);
		deleteRecursive(methodName + ".txt");
		
		// Generate Jimple for debug purposes
		System.out.println("GENERATING TRANSFORMED CLASS FILES");
		MainDriver.main(new String[] {"src/test/resources", "classes.TestClass", methodName, "C"});

		System.out.println("\nGENERATING TRANSFORMED JIMPLE FILES");
		MainDriver.main(new String[] {"src/test/resources", "classes.TestClass", methodName, "J"});
		
		// Run transformed class files
		System.out.println("\nRUNNING TRANSFORMED CLASS FILES");
		File sootOutputFolder = new File("sootOutput/" + methodName);
		File runtimeLibrary = new File("../ch.trick17.rolez.checked.lib/target/ch.trick17.rolez.checked.lib-1.0.0-SNAPSHOT.jar");
		File filesToExecute = new File("sootOutput/" + methodName + "/classes");
		//TODO: Also add annotation processor to classpath
		
		JavaProcessBuilder processBuilder = new JavaProcessBuilder("sootOutput/" + methodName + "/classes/TestClass.class", new String[] { });
		
		try {
			processBuilder.classpath("");
			processBuilder.addClasspath(sootOutputFolder.getCanonicalPath());
			processBuilder.addClasspath(runtimeLibrary.getCanonicalPath());
			processBuilder.addClasspath(filesToExecute.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ProcessBuilder pb = processBuilder.build();
		
		// Transformations are necessary to run it on my local PC -> What happens in CI?
		List<String> command = pb.command();
		command.set(0, "java");
		command.set(command.size()-1, "classes.TestClass");
		pb.command(command);

		// Run process first time for command line output
		try {
			pb.redirectOutput(Redirect.INHERIT);
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Run process a second time by redirecting output to a text file
		try {
			pb.redirectOutput(new File(methodName + ".txt"));
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes a folder located at <code>path</code> and all its content.
	 * @param path
	 */
	private void deleteRecursive(String path) {
		File filePath = new File(path);
		if (!filePath.exists()) return;
		
		if (!filePath.delete()) {
			for (File f : filePath.listFiles()) {
				deleteRecursive(f.getAbsolutePath());
			}
			filePath.delete();
		}
	}
}
