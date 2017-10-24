package rolez.checked.transformer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import rolez.checked.transformer.transformers.CheckingTransformer;
import rolez.checked.transformer.transformers.ClassTransformer;
import rolez.checked.transformer.transformers.TaskCallTransformer;
import rolez.checked.transformer.transformers.WrapperTypeTransformer;
import rolez.checked.transformer.util.ClassMapping;
import rolez.checked.transformer.util.Constants;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

public class MainDriver {
	
	public static void main(String[] args) {

		// Get arguments
		String processDir = args[0];	// Where are the classes to process located
		String mainClass = args[1];		// What is the main class (fully qualified name)
		String outputDirName = args[2];	// Custom output dir name -> output will be located in sootOutput/<outputDirName> folder
		String outputFormat = args[3];	// J = jimple, C = class
		
		setUpSoot(processDir, mainClass, outputDirName, outputFormat);

		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer0", new WrapperTypeTransformer())
		);
		
		// Handles the @Task and @Checked annotated classes and transforms them.
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer1", new ClassTransformer())
		);

		// Transforms calls of tasks
		PackManager.v().getPack("jtp").add(
			new Transform("jtp.transformer0", new TaskCallTransformer())
		);
		
		// Inserts guardings
		PackManager.v().getPack("jtp").add(
			new Transform("jtp.transformer1", new CheckingTransformer())
		);
		
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
		ClassMapping.initializeMapping();
		
	}
}
