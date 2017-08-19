package rolez.checked.transformer;

import soot.PackManager;
import soot.Transform;

public class MainDriver {
	public static void main(String[] args) {
		
		// When using a SceneTransformer we have to work on the wjtp (whole-)program pack
		PackManager.v().getPack("wjtp").add(
			new Transform("wjtp.transformer", new ClassTransformer())
		);
		
		soot.Main.main(args);
	} 
}
