package rolez.checked.transformer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;

import soot.SootClass;
import soot.SourceLocator;
import soot.coffi.parameter_annotation;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

public class ClassWriter {

	public static void write(SootClass c) {
		try {
			String fileName = SourceLocator.v().getFileNameFor(c, Options.output_format_class);
			makeDirectories(fileName);
			
			OutputStream streamOut = new JasminOutputStream(new FileOutputStream(fileName));
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			JasminClass jasminClass = new soot.jimple.JasminClass(c);
			jasminClass.print(writerOut);
			writerOut.flush();
			streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void makeDirectories(String path) {
		File f = new File(path);
		String parentPath = f.getParent();
		(new File(parentPath)).mkdirs();
	}
}
