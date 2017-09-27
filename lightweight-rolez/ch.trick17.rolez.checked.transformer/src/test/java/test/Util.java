package test;

import java.io.File;
import java.io.IOException;
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
	
	static void runProcess(ProcessBuilder pb, Redirect redirect) {
		try {
			pb.redirectOutput(redirect);
			Process p = pb.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	static void runProcess(ProcessBuilder pb, File redirect) {
		try {
			pb.redirectOutput(redirect);
			Process p = pb.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
