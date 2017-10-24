package transformer.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;
import transformer.checking.CheckedConstructor;
import transformer.checking.GuardedRefsMethod;
import transformer.task.MainTaskGenerator;
import transformer.task.TaskGenerator;
import transformer.util.Constants;
import transformer.util.Util;

/**
 * A transformer that processes the user defined classes and outputs code, which
 * conforms Rolez.
 * 
 * @author Michael Giger
 *
 */
public class ClassTransformer extends SceneTransformer {

	static final Logger logger = LogManager.getLogger(ClassTransformer.class);
	
	private SootClass mainClass;
	
	private List<SootClass> generatedInnerClasses = new ArrayList<SootClass>();
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		mainClass = Scene.v().getMainClass();
		processClasses();
	}

	private void processClasses() {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();
		
		for (SootClass c : classesToProcess) 
			transformClass(c);
		
		// Add GuardedRefs methods to all the checked classes
		for (SootClass c : classesToProcess) {
			if (Util.isCheckedClass(c)) {
				GuardedRefsMethod guardedRefs = new GuardedRefsMethod(c);
				c.addMethod(guardedRefs);
			}
		}
		
		addInnerClassesToApplicationClasses();
	}
	
	private void transformClass(SootClass c) {
		logger.debug("Processing class: " + c.getName());
	
		// Generate main task if this is the main class
		if (c.equals(mainClass)) {
			MainTaskGenerator mainTaskGenerator = new MainTaskGenerator(c, c.getMethod("void main(java.lang.String[])"));
			mainTaskGenerator.generateMainTask();
			
			// Remember generated inner class
			this.generatedInnerClasses.add(mainTaskGenerator.getInnerClass());
		}

		if (Util.isCheckedClass(c)) {
			if (Util.isExtendingObject(c)) {
				c.setSuperclass(Constants.CHECKED_CLASS);
				replaceConstructors(c);
			}

			// Process methods which are tasks
			processMethods(c);
		}
	}
	
	/**
	 * Searches for methods annotated with @Task and transforms them accordingly.
	 * @param c
	 */
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			logger.debug("Processing method: " + c.getName() + ":" + m.getName());
			if (Util.isTask(m)) {
				TaskGenerator taskGenerator = new TaskGenerator(c, m);
				taskGenerator.generateTask();
				
				// Remember generated inner class
				this.generatedInnerClasses.add(taskGenerator.getInnerClass());
			}
		}
	}

	/**
	 * Adds all generated inner classes to the application classes to process
	 * them as well in the upcoming phases.
	 */
	private void addInnerClassesToApplicationClasses() {
		for (SootClass c : generatedInnerClasses) {
			logger.debug("Adding " + c + " to application classes.");
			c.setInScene(true);
			c.setApplicationClass();
		}
	}
	
	/**
	 * Replaces the constructors of a class by constructors calling the rolez.checked.lang.Checked constructor
	 * @param c
	 */
	private void replaceConstructors(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			if (m.getName().equals("<init>")) {
				CheckedConstructor checkedConstructor = new CheckedConstructor(m);
				c.removeMethod(m);
				c.addMethod(checkedConstructor);
			}
		}
	}
}
