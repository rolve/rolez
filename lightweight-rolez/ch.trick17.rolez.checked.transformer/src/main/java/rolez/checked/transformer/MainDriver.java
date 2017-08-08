package rolez.checked.transformer;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;

public class MainDriver {
	public static void main(String[] args) {
		
		// When using a SceneTransformer we have to work on the wjtp (whole-)program pack
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer", new ClassTransformer())
		);
		
		Scene.v().addBasicClass(rolez.checked.lang.Checked.class.getCanonicalName(), SootClass.SIGNATURES);
		Scene.v().addBasicClass(rolez.checked.lang.Task.class.getCanonicalName(), SootClass.SIGNATURES);
		
		System.out.println("Starting soot.Main");
		soot.Main.main(args);
	} 
}
