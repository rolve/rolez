package rolez.checked.transformer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.task.InnerClass;
import rolez.checked.transformer.task.InnerClassConstructor;
import rolez.checked.transformer.task.InnerClassRunRolezConcrete;
import rolez.checked.transformer.task.InnerClassRunRolezObject;
import rolez.checked.transformer.task.TaskMethod;
import rolez.checked.transformer.util.Util;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Jimple;


/**
 * This class is used for generating the task methods from the annotated methods
 * in the user code.
 * 
 * @author Michael Giger
 *
 */
public class TaskGenerator {
	
	static final Logger logger = LogManager.getLogger(TaskGenerator.class);
	
	static final Jimple J = Jimple.v();
	
	private SootClass targetClass;
	private SootMethod sourceMethod;
	private SootClass innerClass;
	
	public TaskGenerator(SootClass targetClass, SootMethod sourceMethod) {
		this.targetClass = targetClass;
		this.sourceMethod = sourceMethod;
	}

	public void generateTask() {
		generateInnerClass();
		generateTaskMethod();
	}

	private void generateInnerClass() {
		innerClass = new InnerClass(getClassNameFromMethod(), targetClass, sourceMethod);
		generateInnerClassConstructor();
		generateRunRolezMethods();
	}
	
	private void generateTaskMethod() {
		TaskMethod taskMethod = new TaskMethod(Util.getTaskMethodNameFromMethod(sourceMethod), targetClass, innerClass, sourceMethod);
		targetClass.addMethod(taskMethod);
	}
	
	private void generateInnerClassConstructor() {
		InnerClassConstructor innerClassConstructor = new InnerClassConstructor(innerClass, targetClass, sourceMethod);
		innerClass.addMethod(innerClassConstructor);
	}
	
	private void generateRunRolezMethods() {
		InnerClassRunRolezConcrete concreteMethod = new InnerClassRunRolezConcrete(innerClass, sourceMethod);
		innerClass.addMethod(concreteMethod);
		innerClass.addMethod(new InnerClassRunRolezObject(innerClass, concreteMethod));		
	}
	
	/**
	 * For the source method, which is declared as task, this method does generates a unique
	 * name for the inner class containing the name of the source method and its parameter types.
	 * The parameter types are necessary in case task methods are overloaded.
	 * @return String - unique inner class name
	 */
	private String getClassNameFromMethod() {
		String srcMethodName = sourceMethod.getName();
		List<Type> srcMethodArgs = sourceMethod.getParameterTypes();
		String typeString = "";
		for (Type t : srcMethodArgs) {
			typeString += "$" + sanitizeType(t.toString());
		}
		String className = srcMethodName.substring(0,1).toUpperCase() + srcMethodName.substring(1);
		return targetClass.getName() + "$" + className + typeString;
	}
	
	/**
	 * For type strings, this method replaces the dots which separates package names
	 * with & symbols. Dots in file names are bad ;)
	 * @param type
	 * @return
	 */
	private String sanitizeType(String type) {
		return type.replace(".", "&");
	}
	
	public SootClass getInnerClass() {
		return this.innerClass;
	}
}
