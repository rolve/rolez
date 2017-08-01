package rolez.checked.transformer;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.exceptions.IllegalCheckedAnnotation;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
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

	final static Logger logger = LogManager.getLogger(ClassTransformer.class);
	
	@Override
	protected void internalTransform(String phaseName, Map options) { 
		processClasses();
	}

	private void processClasses() {
		Scene.v().loadClassAndSupport("rolez.checked.lang.Checked");
		
		Chain<SootClass> classes = Scene.v().getApplicationClasses();
		for (SootClass c : classes) {
			logger.debug("Processing class: " + c.getName());
			if (!c.getName().toString().equals("rolez.checked.transformer.test.Test")) {
				continue;
			}
			
			List<Tag> classTags = c.getTags();
			for (Tag t : classTags) {
				if (t instanceof VisibilityAnnotationTag) {
					VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) t;
					for (AnnotationTag aTag : vTag.getAnnotations()) {
						if (aTag.getType().equals("Lrolez/annotation/Checked;")) {
							try {
								if (c.getSuperclass().getName().equals("java.lang.Object")) 
									setCheckedSuperClass(c);
								else {
									// TODO: This should actually never happen since the annotation processor should handle this...
									throw new IllegalCheckedAnnotation("Checked annotations can only be placed at classes without a supertype");
								}
							} catch (IllegalCheckedAnnotation e) {
								e.printStackTrace();
							}
						}
	                }
				}
			}
			
			processMethods(c);
			
			logger.debug("\n" + c.getMethodByName("main").getActiveBody().toString());
		}
	}
	
	private void processMethods(SootClass c) {
		List<SootMethod> methods = c.getMethods();
		for (SootMethod m : methods) {
			List<Tag> methodTags = m.getTags();
			for (Tag t : methodTags) {
				if (t instanceof VisibilityAnnotationTag) {
					VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) t;
					for (AnnotationTag aTag : vTag.getAnnotations()) {
						if (aTag.getType().equals("Lrolez/annotation/Roleztask;")) {
							createTaskMethod(c, m);
						}
	                }
				}
			}
		}
	}
	
	// TODO: Create correct method which returns a task
	private void createTaskMethod(SootClass c, SootMethod m) {
		SootMethod taskMethod = new SootMethod("$" + m.getName() + "Task", m.getParameterTypes(), m.getReturnType(), m.getModifiers());
		taskMethod.setActiveBody(m.retrieveActiveBody());
		c.addMethod(taskMethod);
	}
	
	/**
	 * Sets {@link rolez.checked.lang.Checked} as superclass of class c
	 * 
	 * @param c
	 */
	private void setCheckedSuperClass(SootClass c) {
		logger.debug(c.getSuperclass());
		logger.debug("Set rolez.checked.lang.Checked as superclass of " + c.getName());
		c.setSuperclass(Scene.v().getSootClass("rolez.checked.lang.Checked"));
		logger.debug(c.getSuperclass());
	}
}
