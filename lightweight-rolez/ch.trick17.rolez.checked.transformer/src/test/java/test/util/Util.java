package test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import ch.trick17.javaprocesses.JavaProcessBuilder;

public class Util {
	
	/**
	 * Deletes a folder located at <code>path</code> and all its content.
	 * @param path
	 */
	static void deleteRecursive(String path) {
		File filePath = new File(path);
		if (!filePath.exists()) return;
		
		if (!filePath.delete()) {
			for (File f : filePath.listFiles()) {
				deleteRecursive(f.getAbsolutePath());
			}
			filePath.delete();
		}
	}

	/**
	 * Sets the classpath of a JavaProcessBuilder.
	 * @param pb
	 * @param files
	 * @return
	 */
	static ProcessBuilder setClassPathAndBuild(JavaProcessBuilder pb, File... files) {
		pb.classpath("");
		try {
			for (File f : files) {
				pb.addClasspath(f.getCanonicalPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pb.build();
	}
	
	static void setJavaCommand(ProcessBuilder pb, String testClass) {
		List<String> command = pb.command();
		command.set(0, "java");
		command.set(command.size()-1, testClass);
		pb.command(command);
	}
	
	/**
	 * Method to run a process built by a ProcessBuilder.
	 * @param pb a ProcessBuilder object which is all set up.
	 * @param redirect a Redirect to determine output destination.
	 */
	static void runProcess(ProcessBuilder pb, Redirect redirect) {
		try {
			pb.redirectErrorStream(true);
			pb.redirectOutput(redirect);
			Process p = pb.start();
			// Block until finished
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to run a process built by a ProcessBuilder.
	 * @param pb a ProcessBuilder object which is all set up.
	 * @param redirect a File to determine output destination.
	 */
	static void runProcess(ProcessBuilder pb, File redirect) {
		try {
			pb.redirectErrorStream(true);
			pb.redirectOutput(redirect);
			Process p = pb.start();
			// Block until finished
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read a file into a String, used to compare the output to the expected output.
	 */
	static String readFile(File file) {
		try {
			FileInputStream fin = new FileInputStream(file);
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
			StringBuilder sb = new StringBuilder();
			String thisLine;
			while ((thisLine = myInput.readLine()) != null) {  
				sb.append(thisLine);
			}
			myInput.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
