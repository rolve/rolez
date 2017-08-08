package rolez.checked.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.task.InnerClass;
import rolez.checked.transformer.task.InnerClassConstructor;
import rolez.checked.transformer.task.InnerClassRunRolezConcrete;
import rolez.checked.transformer.task.InnerClassRunRolezObject;
import rolez.checked.transformer.task.TaskMethod;
import soot.Printer;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;


/**
 * This class is used for generating the task methods from the annotated methods
 * in the user code.
 * 
 * @author Michael Giger
 *
 */
public class TaskGenerator {
	
	static final Logger logger = LogManager.getLogger(TaskGenerator.class);
	
	private SootClass targetClass;
	private SootMethod sourceMethod;
	private SootClass innerClass;
	
	public TaskGenerator(SootClass targetClass, SootMethod sourceMethod) {
		this.targetClass = targetClass;
		this.sourceMethod = sourceMethod;
	}

	public void generateMethod() {
		generateInnerClass();
		generateTaskMethod();
		
		// TODO: Add tags to outer class s.t. the outer class knows about the newly created inner class and vice versa!
				// Outer class should contain tag of the following form: [inner=rolez/checked/transformer/test/Test$1, outer=null, name=null,flags=0]
				// Inner class should contain tags of the following form: Signature: Lrolez/checked/lang/Task<Ljava/lang/Void;>;
				//                                                        Enclosing Class: rolez/checked/transformer/test/Test Enclosing Method: $testTask Sig: (Lrolez/checked/transformer/test/A;Lrolez/checked/transformer/test/A;)Lrolez/checked/lang/Task;
				//                                                        [inner=rolez/checked/transformer/test/Test$1, outer=null, name=null,flags=0]
//				for (Tag t : targetClass.getTags()) {
//					logger.debug(t);
//				}
		
		writeJimple();
		writeClass();
	}
	
	private void generateInnerClass() {
		innerClass = new InnerClass(getClassNameFromMethod(), targetClass, sourceMethod);
		generateInnerClassConstructor();
		generateRunRolezMethods();
		
		
	}
	
	private void generateTaskMethod() {
		TaskMethod taskMethod = new TaskMethod(getTaskMethodNameFromMethod(), targetClass, innerClass, sourceMethod);
		targetClass.addMethod(taskMethod);
	}
	
	private void writeClass() {
		logger.debug("Writing class file for inner class");
		try {
			String fileName = SourceLocator.v().getFileNameFor(innerClass, Options.output_format_class);
			OutputStream streamOut = new JasminOutputStream(new FileOutputStream(fileName));
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			JasminClass jasminClass = new soot.jimple.JasminClass(innerClass);
			jasminClass.print(writerOut);
			writerOut.flush();
			streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeJimple() {
		logger.debug("Writing jimple file for inner class");
		try {
			String fileName = SourceLocator.v().getFileNameFor(innerClass, Options.output_format_jimple);
			OutputStream streamOut;
				streamOut = new FileOutputStream(fileName);
			
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			Printer.v().printTo(innerClass, writerOut);
			writerOut.flush();
			streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	private String getTaskMethodNameFromMethod() {
		String srcMethodName = sourceMethod.getName();
		return "$" + srcMethodName + "Task";
	}
	
	private String getClassNameFromMethod() {
		String srcMethodName = sourceMethod.getName();
		String className = srcMethodName.substring(0,1).toUpperCase() + srcMethodName.substring(1);
		return targetClass.getName() + "$" + className;
	}
}
