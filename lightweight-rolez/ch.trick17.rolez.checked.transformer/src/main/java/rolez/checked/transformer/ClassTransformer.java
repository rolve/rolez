package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Checked;
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
	
	SootClass checkedClass;
	
	static final String ROLEZTASK_ANNOTATION = "Lrolez/annotation/Roleztask;";
	static final String CHECKED_ANNOTATION = "Lrolez/annotation/Checked;";
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		
		// Load useful classes
		checkedClass = Scene.v().loadClassAndSupport(Checked.class.getCanonicalName());
		
		// Start transformation
		processClasses();
	}

	private void processClasses() {
		
		Chain<SootClass> classes = Scene.v().getApplicationClasses();
		
		for (SootClass c : classes) {
			logger.debug("Processing class: " + c.getName());
			
			List<Tag> classTags = c.getTags();
			for (Tag t : classTags) {
				if (t instanceof VisibilityAnnotationTag) {
					VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) t;
					for (AnnotationTag aTag : vTag.getAnnotations()) {
						if (aTag.getType().equals(CHECKED_ANNOTATION)) {
							try {
								if (c.getSuperclass().getName().equals("java.lang.Object")) 
									c.setSuperclass(checkedClass);
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
			
			SootClass anonymousClass = Scene.v().getSootClass("rolez.checked.transformer.test.Test$1");
			for (SootMethod m : anonymousClass.getMethods()) {
				System.out.println(m.retrieveActiveBody());
			}
			
			processMethods(c);
		}
	}
	
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			System.out.println(m.retrieveActiveBody());
			for (Tag t : m.getTags()) {
				if (t instanceof VisibilityAnnotationTag) {
					for (AnnotationTag aTag : ((VisibilityAnnotationTag)t).getAnnotations()) {
						if (aTag.getType().equals(ROLEZTASK_ANNOTATION)) {
							TaskMethodGenerator taskGenerator = new TaskMethodGenerator(c, m);
							taskGenerator.generateMethod();
						}
	                }
				}
			}
		}
	}
}
