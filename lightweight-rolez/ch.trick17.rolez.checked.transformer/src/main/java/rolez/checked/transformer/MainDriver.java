package rolez.checked.transformer;

import java.util.Collections;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

public class MainDriver {
	public static void main(String[] args) {
		
		setUpSoot();
		
		// When using a SceneTransformer we have to work on the wjtp (whole-)program pack
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer", new ClassTransformer())
		);

        PackManager.v().runPacks();
	}
	
	private static void setUpSoot() {
		G.reset();
		
		Options opts = Options.v();
		opts.set_process_dir(Collections.singletonList("target/classes"));
		opts.set_soot_classpath("../ch.trick17.rolez.checked.lib/target/ch.trick17.rolez.checked.lib-1.0.0-SNAPSHOT.jar");
		opts.set_prepend_classpath(true);
		opts.set_allow_phantom_refs(true);
		opts.set_whole_program(true);
		opts.set_main_class("test.Test");
		opts.setPhaseOption("cg", "enabled:false");
		
		Scene.v().loadNecessaryClasses();
	}
}
