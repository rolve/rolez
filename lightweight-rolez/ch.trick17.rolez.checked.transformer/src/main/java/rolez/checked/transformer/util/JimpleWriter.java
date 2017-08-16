package rolez.checked.transformer.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import soot.Printer;
import soot.SootClass;
import soot.SourceLocator;
import soot.options.Options;

public class JimpleWriter {

	public static void write(SootClass c) {
		try {
			String fileName = SourceLocator.v().getFileNameFor(c, Options.output_format_jimple);
			OutputStream streamOut;
				streamOut = new FileOutputStream(fileName);
			
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			Printer.v().printTo(c, writerOut);
			writerOut.flush();
			streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
