package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.checked.CheckedConstructor;
import rolez.checked.transformer.checked.GuardedRefsMethod;
import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.Util;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;

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
		
		logger.debug("The following classes are transformed: " + classesToProcess);
		for (SootClass c : classesToProcess) {
			processClass(c);
		}
		
		// Add all generated inner classes to the application classes
		// (is necessary because we want the next phase to transform them as well)
		addInnerClassesToApplicationClasses();
	}
	
	private void processClass(SootClass c) {
		logger.debug("Processing class: " + c.getName());
	
		if (c.equals(mainClass)) {
			MainTaskGenerator mainTaskGenerator = new MainTaskGenerator(c, c.getMethodByName("main"));
			mainTaskGenerator.generateMainTask();
			
			// Remember generated inner class
			this.generatedInnerClasses.add(mainTaskGenerator.getInnerClass());
		}
		
		if (Util.hasCheckedAnnotation(c)) {
			c.setSuperclass(Constants.CHECKED_CLASS);
			
			// Replace constructors with one that calls the Checked constructor
			for (SootMethod m : c.getMethods()) {
				if (m.getName().equals("<init>")) {
					CheckedConstructor checkedConstructor = new CheckedConstructor(m);
					c.removeMethod(m);
					c.addMethod(checkedConstructor);
				}
			}
			
			// Generate the guardedRefs method
			GuardedRefsMethod guardedRefs = new GuardedRefsMethod(c);
			c.addMethod(guardedRefs);
		}
		
		// Search for methods which have the @Roleztask annotation
		processMethods(c);
	}
	
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			logger.debug("Processing method: " + c.getName() + ":" + m.getName());
			m.retrieveActiveBody();
			if (Util.hasRoleztaskAnnotation(m)) {
				TaskGenerator taskGenerator = new TaskGenerator(c, m);
				taskGenerator.generateTask();
				
				// Remember generated inner class
				this.generatedInnerClasses.add(taskGenerator.getInnerClass());
			}
		}
	}

	private void addInnerClassesToApplicationClasses() {
		for (SootClass c : generatedInnerClasses) {
			logger.debug("Adding " + c + " to application classes.");
			c.setInScene(true);
			c.setApplicationClass();
		}
	}
}
