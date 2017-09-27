package test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

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
	
	Pipeline(String methodName, String mainClass) {
		this.methodName = methodName;
		this.mainClass = mainClass;
		this.srcPath = new File("src/test/resources");
		this.compilePath = new File("target/test-compile");
		this.annotationProcessor = new File("../ch.trick17.rolez.checked.annotation/target/ch.trick17.rolez.checked.annotation-1.0.0-SNAPSHOT-jar-with-dependencies.jar");
		this.sootOutputFolder = new File("sootOutput/" + methodName);
		this.filesToExecute = new File("sootOutput/" + methodName + "/classes");
	}
	
	public void run() {
		
		// Delete output folder and output file
		Util.deleteRecursive("sootOutput/" + methodName);
		Util.deleteRecursive(methodName + ".txt");
		
		// Compile the sources from src/test/resources
		compileSources();
		
		// Generate Jimple for debug purposes
		System.out.println("GENERATING TRANSFORMED CLASS FILES");
		MainDriver.main(new String[] {compilePath.getAbsolutePath(), mainClass, methodName, "C"});

		System.out.println("\nGENERATING TRANSFORMED JIMPLE FILES");
		MainDriver.main(new String[] {compilePath.getAbsolutePath(), mainClass, methodName, "J"});
		
		// Run transformed class files
		
		JavaProcessBuilder processBuilder = new JavaProcessBuilder("sootOutput/" + methodName + "/classes/TestClass.class", new String[] { });
		ProcessBuilder pb = Util.setClassPathAndBuild(processBuilder, sootOutputFolder, annotationProcessor, filesToExecute);
		
		// Transformations are necessary to run it on my local PC -> What happens in CI?
		Util.setJavaCommand(pb, mainClass);
		System.out.println(pb.command());

		// Run process first time for command line output
		System.out.println("\nRUNNING TRANSFORMED CLASS FILES (console output)");
		Util.runProcess(pb, Redirect.INHERIT);
		
		// Run process a second time by redirecting output to a text file
		System.out.println("\nRUNNING TRANSFORMED CLASS FILES (file output)");
		Util.runProcess(pb, new File(methodName + ".txt"));
		
		System.out.println("\n\n");
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
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
		// Find and add files in subdirectories
		for (File f : currentDir.listFiles()) {
			if (f.isDirectory()) {
				classes.addAll(findClassesToCompile(f));
			}
		}
		
		return classes;
	}
}
