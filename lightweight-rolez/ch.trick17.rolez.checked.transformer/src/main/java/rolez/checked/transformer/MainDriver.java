package rolez.checked.transformer;

import java.io.File;
import java.util.Collections;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

public class MainDriver {
	
	public static void main(String[] args) {
		
		setUpSoot();
		
		// Handles the @Roleztask and @Checked annotated classes and transforms them.
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer0", new ClassTransformer())
		);

		// Transforms calls of tasks
		PackManager.v().getPack("jtp").add(
			new Transform("jtp.transformer1", new TaskCallTransformer())
		);
		
		// Inserts guardings
		PackManager.v().getPack("jtp").add(
			new Transform("jtp.transformer2", new CheckingTransformer())
		);
		
        PackManager.v().runPacks();

        System.out.println("Finished transformation");
	}
	
	private static void setUpSoot() {
		G.reset();
		
		Options opts = Options.v();
		
		// TODO: The process dir should be an argument to the transformer
		File f = new File("../ch.trick17.rolez.checked.transformer.test/target/classes");
		opts.set_process_dir(Collections.singletonList(f.getAbsolutePath()));
		
		opts.set_soot_classpath("../ch.trick17.rolez.checked.lib/target/ch.trick17.rolez.checked.lib-1.0.0-SNAPSHOT.jar");
		opts.set_prepend_classpath(true);
		opts.set_allow_phantom_refs(true);
		opts.set_whole_program(true);
		opts.set_main_class("test.Test");
		opts.setPhaseOption("cg", "enabled:false");
		
		Scene.v().loadNecessaryClasses();
	}
}
