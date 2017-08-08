package rolez.checked.transformer;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Checked;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.ReturnStmt;
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
	SootClass objectClass;
	
	static final String ROLEZTASK_ANNOTATION = "Lrolez/annotation/Roleztask;";
	static final String CHECKED_ANNOTATION = "Lrolez/annotation/Checked;";
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		
		// Load useful classes
		checkedClass = Scene.v().loadClassAndSupport(Checked.class.getCanonicalName());
		objectClass = Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
		
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
					for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) {
						if (aTag.getType().equals(CHECKED_ANNOTATION))
							c.setSuperclass(checkedClass);							
	                }
				}
			}
			
			processMethods(c);

			// The following code is used to display stuff during development
			SootClass anonymousClass = Scene.v().getSootClass("rolez.checked.transformer.test.Test$1");
			/*
			for (Tag t : anonymousClass.getTags()) {
				logger.debug(t);
			}
			for (SootMethod m : anonymousClass.getMethods()) {
				if (m.getName().equals("runRolez")) {
					logger.debug("\n" + m.getDeclaringClass().toString() + "\n" + m.retrieveActiveBody());
					for (Unit u : m.getActiveBody().getUnits()) {
						if (u instanceof ReturnStmt) {
							ReturnStmt rs =(ReturnStmt)u;
							logger.debug(u.getClass().toString() + rs.getOp().getClass());
						}
					}
				}
			}
			*/
		}
	}
	
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			logger.debug("\n" + m.retrieveActiveBody().toString());
			for (Tag t : m.getTags()) {
				if (t instanceof VisibilityAnnotationTag) {
					for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) {
						if (aTag.getType().equals(ROLEZTASK_ANNOTATION)) {
							TaskGenerator taskGenerator = new TaskGenerator(c, m);
							taskGenerator.generateMethod();
						}
	                }
				}
			}
		}
	}
}
