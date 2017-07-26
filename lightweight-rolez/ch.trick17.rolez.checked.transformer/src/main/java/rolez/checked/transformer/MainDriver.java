package rolez.checked.transformer;

import soot.Pack;
import soot.PackManager;
import soot.Transform;

public class MainDriver {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java MainDriver [options] classname");
			System.exit(0);
		}
		
		Pack jtp = PackManager.v().getPack("jtp");
		jtp.add(new Transform("jtp.instrumenter", new Transformer()));

		for (String arg : args) {
			System.out.print(arg + " ");
		}
		System.out.println("");
		
		soot.Main.main(args);
	}
}
