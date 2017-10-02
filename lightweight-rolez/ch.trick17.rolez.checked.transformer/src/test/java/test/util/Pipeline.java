package test.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static junit.framework.Assert.*;
import ch.trick17.javaprocesses.JavaProcessBuilder;
import rolez.checked.transformer.MainDriver;

public class Pipeline {

	String methodName;
	String mainClass;
	
	File srcPath;
	File compilePath;
	File annotationProcessor;
	File sootOutputFolder; 
	File filesToExecute;
	File testLogPath;
	File expectedOutputFile;
	
	public Pipeline(String methodName, String mainClass) {
		this.methodName = methodName;
		this.mainClass = mainClass;
		
		this.srcPath = new File("testClasses/" + methodName);
		this.compilePath = new File("testCompile/" + methodName);
		this.annotationProcessor = new File("../ch.trick17.rolez.checked.annotation/target/ch.trick17.rolez.checked.annotation-1.0.0-SNAPSHOT-jar-with-dependencies.jar");
		this.sootOutputFolder = new File("sootOutput/" + methodName);
		this.filesToExecute = new File("sootOutput/" + methodName);
		this.testLogPath = new File("testLogs/" + methodName);
		this.expectedOutputFile = new File("testClasses/" + methodName + "/expected.out");
	}
	
	public void runDefault(boolean doAssertion, boolean generateJimple) {
		
		run(generateJimple);
		
		// Compare file output to expected output
		if (doAssertion)
			assertEquals(Util.readFile(expectedOutputFile), Util.readFile(new File(testLogPath.getAbsolutePath() + "/result.out")));
	}
	
	public void runExpectNonSufficientRoleError(boolean generateJimple) {
		run(generateJimple);
		
		String output = Util.readFile(new File(testLogPath.getAbsolutePath() + "/result.out"));
		String[] split = output.split(" ");
		assertEquals("rolez.checked.lang.NonSufficentRoleException:", split[1]);
	}
	
	private void run(boolean generateJimple) {
		// Delete output folder and output file
		Util.deleteRecursive(sootOutputFolder.getAbsolutePath());
		Util.deleteRecursive(testLogPath.getAbsolutePath());
		
		// Compile sources
		compileSources();
		
		// Generate Jimple for debug purposes
		System.out.println("GENERATING TRANSFORMED CLASS FILES");
		System.out.println(mainClass);
		MainDriver.main(new String[] {compilePath.getAbsolutePath(), mainClass, methodName, "C"});

		if (generateJimple) {
			System.out.println("\nGENERATING TRANSFORMED JIMPLE FILES");
			MainDriver.main(new String[] {compilePath.getAbsolutePath(), mainClass, methodName, "J"});
		}
		
		// Run transformed class files
		JavaProcessBuilder processBuilder = new JavaProcessBuilder(sootOutputFolder.getAbsolutePath() + mainClassToPath(mainClass), new String[] { });
		ProcessBuilder pb = Util.setClassPathAndBuild(processBuilder, sootOutputFolder, annotationProcessor, filesToExecute);
		
		// Transformations are necessary to run process on windows because of whitespace in paths
		Util.setJavaCommand(pb, mainClass);
		System.out.println(pb.command());
		
		// Run transformed program
		System.out.println("\nRUNNING TRANSFORMED CLASS FILES (file output)");
		createLogOutputDirs();
		Util.runProcess(pb, new File(testLogPath.getAbsolutePath() + "/result.out"));
		
		System.out.println("\n\n");
	}
	
	private void cleanTestCompileDir() {

		Util.deleteRecursive(compilePath.getAbsolutePath());
		try {
			Files.createDirectories(compilePath.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<File> findClassesToCompile(File currentDir) {
		ArrayList<File> classes = new ArrayList<File>();
		
		// Find and add files in current directory
		File[] javafiles = currentDir.listFiles(
			new FilenameFilter() { 
	            public boolean accept(File dir, String filename) { 
	            	return filename.endsWith(".java"); 
	            }
			}
		);
		classes.addAll(Arrays.asList(javafiles));
		
		// Find and add files in sub-directories
		for (File f : currentDir.listFiles()) {
			if (f.isDirectory()) {
				classes.addAll(findClassesToCompile(f));
			}
		}
		
		return classes;
	}
	
	private void compileSources() {
		List<File> classesToCompile = findClassesToCompile(srcPath);

		cleanTestCompileDir();
		
		ArrayList<String> command = new ArrayList<String>();
		command.add("javac");
		command.add("-d");
		command.add(compilePath.getAbsolutePath());
		for (File f : classesToCompile) {
			command.add(f.getAbsolutePath());
		}
		command.add("-cp");
		command.add(annotationProcessor.getAbsolutePath());
		
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.INHERIT);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				throw new RuntimeException("Compilation failed");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void createLogOutputDirs() {
		try {
			Files.createDirectories(testLogPath.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String mainClassToPath(String mainClass) {
		return "/" + mainClass.replaceAll(".", "/") + ".class";
	}
}
