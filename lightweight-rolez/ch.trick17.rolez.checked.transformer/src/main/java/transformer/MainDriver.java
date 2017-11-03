package transformer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;
import transformer.transformers.CheckingTransformer;
import transformer.transformers.ClassTransformer;
import transformer.transformers.TaskCallTransformer;
import transformer.transformers.TaskIdTransformer;
import transformer.util.Constants;

public class MainDriver {
	
	public static void main(String[] args) {

		// Get arguments
		String processDir = args[0];	// Where are the classes to process located
		String mainClass = args[1];		// What is the main class (fully qualified name)
		String outputDirName = args[2];	// Custom output dir name -> output will be located in sootOutput/<outputDirName> folder
		String outputFormat = args[3];	// J = jimple, C = class
		
		setUpSoot(processDir, mainClass, outputDirName, outputFormat);
		
		// Start transformation
        soot.Main.main(new String[] {});
        
        System.out.println("Finished transformation");
	}
	
	private static void setUpSoot(String processDir, String mainClass, String outputDirName, String outputFormat) {
		G.reset();
		
		Options opts = Options.v();
		
		opts.set_unfriendly_mode(true);
		
		// Options depending on program arguments
		File f = new File(processDir);
		try {
			opts.set_process_dir(Collections.singletonList(f.getCanonicalPath()));
			opts.set_main_class(mainClass);
			opts.set_output_dir("sootOutput/" + outputDirName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		opts.set_soot_classpath("../ch.trick17.rolez.checked.annotation/target/ch.trick17.rolez.checked.annotation-1.0.0-SNAPSHOT-jar-with-dependencies.jar");
		opts.set_prepend_classpath(true);
		opts.set_allow_phantom_refs(true);
		opts.set_whole_program(true);
		opts.setPhaseOption("cg", "enabled:false");
		
		if (outputFormat.equals("J")) 
			opts.set_output_format(Options.output_format_J);
		if (outputFormat.equals("C"))
			opts.set_output_format(Options.output_format_class);

		Scene.v().loadNecessaryClasses();
		
		// Class constants and available wrapper classes have to be newly resolved in every run
		Constants.resolveClasses();
		
		//Register transformers		
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer0", new ClassTransformer())
		);
		
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer1", new TaskIdTransformer())
		);

		PackManager.v().getPack("jtp").add(
			new Transform("jtp.transformer0", new TaskCallTransformer())
		);
		
		PackManager.v().getPack("jtp").add(
			new Transform("jtp.transformer1", new CheckingTransformer())
		);
	}
}
