package rolez.checked.transformer;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.exceptions.IllegalCheckedAnnotation;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.Chain;

public class ClassTransformer extends SceneTransformer {

	final static Logger logger = LogManager.getLogger(ClassTransformer.class);
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		
		Scene.v().loadClassAndSupport("rolez.checked.lang.Checked");
			
		Chain<SootClass> classes = Scene.v().getApplicationClasses();
		for (SootClass c : classes) {
			if (!c.getName().toString().equals("rolez.checked.transformer.test.Test")) {
				continue;
			}
			
			List<Tag> classTags = c.getTags();
			for (Tag t : classTags) {
				if (t instanceof VisibilityAnnotationTag) {
					VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) t;
					for (AnnotationTag aTag : vTag.getAnnotations()) {
						logger.debug(aTag.getType());
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
			
		}
	}
	
	private void setCheckedSuperClass(SootClass c) {
		logger.debug(c.getSuperclass());
		logger.debug("Set rolez.checked.lang.Checked as superclass of " + c.getName());
		c.setSuperclass(Scene.v().getSootClass("rolez.checked.lang.Checked"));
		logger.debug(c.getSuperclass());
	}
}
