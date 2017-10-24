package transformer.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Modifier;
import soot.SootClass;
import soot.SootField;
import transformer.util.Constants;

public class MainInnerClass extends SootClass {

	static final Logger logger = LogManager.getLogger(TaskGenerator.class);

	private SootClass outerClass;
	
	public MainInnerClass(String name, SootClass outerClass) {
		super(name);
		this.outerClass = outerClass;
		
		this.setSuperclass(Constants.TASK_CLASS);
		this.setOuterClass(outerClass);
		generateInnerClassFields();
	}
	
	private void generateInnerClassFields() {
		SootField outerClassReference = new SootField("val$f0", outerClass.getType(), Modifier.FINAL);
		this.addField(outerClassReference);
	}
}
