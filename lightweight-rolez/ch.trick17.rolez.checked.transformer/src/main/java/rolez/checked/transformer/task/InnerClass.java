package rolez.checked.transformer.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Task;
import rolez.checked.transformer.TaskGenerator;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class InnerClass extends SootClass {

	static final Logger logger = LogManager.getLogger(TaskGenerator.class);
	
	static final SootClass TASK_CLASS = Scene.v().loadClassAndSupport(Task.class.getCanonicalName());

	private SootClass outerClass;
	private SootMethod sourceMethod;
	
	public InnerClass(String name, SootClass outerClass, SootMethod sourceMethod) {
		super(name);
		this.outerClass = outerClass;
		this.sourceMethod = sourceMethod;
		
		this.setSuperclass(TASK_CLASS);
		this.setOuterClass(outerClass);
		generateInnerClassFields();
	}
	
	private void generateInnerClassFields() {
		SootField outerClassReference = new SootField("this$0", outerClass.getType());
		this.addField(outerClassReference);
	
		// Add a field for every source method parameter
		for (int i=0; i<sourceMethod.getParameterCount(); i++) {
			SootField paramField = new SootField("val$f" + Integer.toString(i), sourceMethod.getParameterType(i));
			this.addField(paramField);
		}
	}
}
