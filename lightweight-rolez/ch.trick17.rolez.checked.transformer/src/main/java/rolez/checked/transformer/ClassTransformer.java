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

	static final Logger logger = LogManager.getLogger(ClassTransformer.class);
	
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
			
			if (hasCheckedAnnotation(c))
				c.setSuperclass(checkedClass);
			
			processMethods(c);
		}
	}
	
	private boolean hasCheckedAnnotation(SootClass c) {
		List<Tag> classTags = c.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(CHECKED_ANNOTATION))
						return true;
		return false;
	}
	
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			logger.debug("Processing method: " + c.getName() + ":" + m.getName());
			if (hasRoleztaskAnnotation(m)) {
				TaskGenerator taskGenerator = new TaskGenerator(c, m);
				taskGenerator.generateMethod();
			}
			
		}
	}
	
	private boolean hasRoleztaskAnnotation(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(ROLEZTASK_ANNOTATION)) 
						return true;
		return false;
	}
}
