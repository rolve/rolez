package rolez.checked.transformer;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.checked.CheckedConstructor;
import rolez.checked.transformer.checked.GuardedRefsMethod;
import rolez.checked.transformer.util.ClassWriter;
import rolez.checked.transformer.util.JimpleWriter;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.SootResolver;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
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
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		mainClass = Scene.v().getMainClass();
		
		// Start transformation
		processClasses();
	}

	private void processClasses() {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();
		
		logger.debug(classesToProcess);
		for (SootClass c : classesToProcess) {
			processClass(c);
		}
	}
	
	private void processClass(SootClass c) {
		logger.debug("Processing class: " + c.getName());
		
		SootResolver.v().resolveClass(c.getName(), SootClass.SIGNATURES);
		
		if (c.equals(mainClass)) {
			MainTaskGenerator mainTaskGenerator = new MainTaskGenerator(c, c.getMethodByName("main"));
			mainTaskGenerator.generateMethod();
		}
		
		if (hasCheckedAnnotation(c)) {
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
		
		JimpleWriter.write(c);
		ClassWriter.write(c);
	}
	
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			logger.debug("Processing method: " + c.getName() + ":" + m.getName());
			m.retrieveActiveBody();
			if (hasRoleztaskAnnotation(m)) {
				TaskGenerator taskGenerator = new TaskGenerator(c, m);
				taskGenerator.generateMethod();
			}
		}
	}

	private boolean hasCheckedAnnotation(SootClass c) {
		List<Tag> classTags = c.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.CHECKED_ANNOTATION))
						return true;
		return false;
	}
	
	private boolean hasRoleztaskAnnotation(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.ROLEZTASK_ANNOTATION)) 
						return true;
		return false;
	}
}
