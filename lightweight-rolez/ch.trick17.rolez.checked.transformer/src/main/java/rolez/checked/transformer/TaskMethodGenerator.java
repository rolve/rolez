package rolez.checked.transformer;

import java.awt.PageAttributes.OriginType;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Task;
import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.util.Chain;
import soot.util.JasminOutputStream;
import soot.util.NumberedString;


/**
 * This class is used for generating the task methods from the annotated methods
 * in the user code.
 * 
 * @author Michael Giger
 *
 */
public class TaskMethodGenerator {

	final static Logger logger = LogManager.getLogger(ClassTransformer.class);
	
	private SootClass targetClass;
	private SootMethod srcMethod;
	private SootClass innerClass;

	private SootClass taskClass;
	private SootClass objectClass;
	
	public TaskMethodGenerator(SootClass targetClass, SootMethod srcMethod) {
		this.targetClass = targetClass;
		this.srcMethod = srcMethod;

		taskClass = Scene.v().loadClassAndSupport(Task.class.getCanonicalName());
		objectClass = Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
	}

	// TODO: How to handle generic tasks..? Generics are a java source-level feature...
	public void generateMethod() {
		generateInnerClass();
	}
	
	private void generateInnerClass() {
		innerClass = new SootClass(getClassNameFromMethod());
		innerClass.setSuperclass(taskClass);
		generateInnerClassFields();
		generateInnerClassConstructor();
		
		/*
		SootMethod runRolezMethod = new SootMethod("runRolez", new ArrayList<>(), srcMethod.getReturnType(), Modifier.PROTECTED);
		runRolezMethod.setActiveBody(srcMethod.retrieveActiveBody());
		innerTaskClass.addMethod(runRolezMethod);
		innerTaskClass.setOuterClass(targetClass);
		Scene.v().addClass(innerTaskClass);
		*/
	}
	
	private void generateInnerClassFields() {
		SootField outerClassReference = new SootField("this$0", targetClass.getType());
		innerClass.addField(outerClassReference);
	
		// Add a field for every source method parameter
		for (int i=0; i<srcMethod.getParameterCount(); i++) {
			SootField paramField = new SootField("val$f" + Integer.toString(i), srcMethod.getParameterType(i));
			innerClass.addField(paramField);
		}
	}
	
	private void generateInnerClassConstructor() {
		logger.debug("Generating inner class constructor");
		
		// Useful variables
		ArrayType objectArrayType = ArrayType.v(RefType.v(objectClass),1);
		RefType targetClassType = targetClass.getType();
		RefType innerClassType = innerClass.getType();
		int offset = 5;
		int numberOffset = 4;
		
		// List of constructor parameters
		List<Type> parameterTypes = new ArrayList<Type>();
		
		// Add "this"
		parameterTypes.add(targetClassType);
		
		// Add three Object array types to pass passed, shared and pure objects
		parameterTypes.add(objectArrayType);
		parameterTypes.add(objectArrayType);
		parameterTypes.add(objectArrayType);
		
		// TODO: Don't add the boolean $asTask to the list...
		// Add all original method parameters
		List<Type> srcParameterTypes = srcMethod.getParameterTypes();
		for (Type t : srcParameterTypes) {
			parameterTypes.add(t);
		}
		
		// Create constructor
		SootMethod constructor = new SootMethod(
				"<init>",
				parameterTypes,
				VoidType.v()
			);		
		
		innerClass.addMethod(constructor);
		
		JimpleBody body = Jimple.v().newBody(constructor);
		constructor.setActiveBody(body);
		
		// All local variables that have to be added to the body
		List<Local> locals = new ArrayList<Local>();
		locals.add(Jimple.v().newLocal("r0", innerClassType));
		locals.add(Jimple.v().newLocal("r1", targetClassType));
		locals.add(Jimple.v().newLocal("r2", objectArrayType));
		locals.add(Jimple.v().newLocal("r3", objectArrayType));
		locals.add(Jimple.v().newLocal("r4", objectArrayType));
		for (int i=0; i<srcParameterTypes.size(); i++) {
			locals.add(Jimple.v().newLocal("r"+Integer.toString(offset + i), srcParameterTypes.get(i)));
		}
		
		// Add the locals to the body
		Chain<Local> bodyLocals = body.getLocals();
		for (Local l : locals)
			bodyLocals.add(l);
		
		Chain<Unit> units = body.getUnits();
		units.add(Jimple.v().newIdentityStmt(locals.get(0), Jimple.v().newThisRef(innerClassType)));
		units.add(Jimple.v().newIdentityStmt(locals.get(1), Jimple.v().newParameterRef(targetClassType, 0)));
		units.add(Jimple.v().newIdentityStmt(locals.get(2), Jimple.v().newParameterRef(objectArrayType, 1)));
		units.add(Jimple.v().newIdentityStmt(locals.get(3), Jimple.v().newParameterRef(objectArrayType, 2)));
		units.add(Jimple.v().newIdentityStmt(locals.get(4), Jimple.v().newParameterRef(objectArrayType, 3)));
		for (int i=0; i<srcParameterTypes.size(); i++) {
			units.add(Jimple.v().newIdentityStmt(locals.get(i+offset), Jimple.v().newParameterRef(srcParameterTypes.get(i), i+numberOffset)));
		}
		
		// Set field field for outer class ref
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(locals.get(0), innerClass.getFieldByName("this$0").makeRef()), locals.get(1)));
		
		// Set fields for method parameters
		for (int i=0; i<srcMethod.getParameterCount(); i++) {
			units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(locals.get(0), innerClass.getFieldByName("val$f" + Integer.toString(i)).makeRef()), locals.get(offset + i)));
		}
		
		// Add the call to superclass constructor
		units.add(Jimple.v().newInvokeStmt(
				Jimple.v().newSpecialInvokeExpr(
						locals.get(0), 
						taskClass.getMethodByName("<init>").makeRef(), 
						Arrays.asList(new Local[] {
								locals.get(2), 
								locals.get(3), 
								locals.get(4)})
		)));
				
		logger.debug(constructor.getActiveBody().toString());	
	}
	
	private String getClassNameFromMethod() {
		String originMethodName = srcMethod.getName();
		String className = originMethodName.substring(0,1).toUpperCase() + originMethodName.substring(1);
		return targetClass.getName() + "$" + className;
	}
}
