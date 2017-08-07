package rolez.checked.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Task;
import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.IdentityStmt;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ThisRef;
import soot.options.Options;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import soot.util.Chain;
import soot.util.JasminOutputStream;


/**
 * This class is used for generating the task methods from the annotated methods
 * in the user code.
 * 
 * @author Michael Giger
 *
 */
public class TaskGenerator {

	// TODO: Write a class that represents the inner class and contains all SootClasses and SootMethods 
	//       generated here.
	
	final static Logger logger = LogManager.getLogger(TaskGenerator.class);
	
	private SootClass targetClass;
	private SootMethod srcMethod;
	private SootClass innerClass;

	private SootClass taskClass;
	private SootClass objectClass;
	
	public TaskGenerator(SootClass targetClass, SootMethod srcMethod) {
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
		generateRunRolezMethods();
		innerClass.setOuterClass(targetClass);
		
		// TODO: Add tags to outer class s.t. the outer class knows about the newly created inner class and vice versa!
		// Outer class should contain tag of the following form: [inner=rolez/checked/transformer/test/Test$1, outer=null, name=null,flags=0]
		// Inner class should contain tags of the following form: Signature: Lrolez/checked/lang/Task<Ljava/lang/Void;>;
		//                                                        Enclosing Class: rolez/checked/transformer/test/Test Enclosing Method: $testTask Sig: (Lrolez/checked/transformer/test/A;Lrolez/checked/transformer/test/A;)Lrolez/checked/lang/Task;
		//                                                        [inner=rolez/checked/transformer/test/Test$1, outer=null, name=null,flags=0]
		for (Tag t : targetClass.getTags()) {
			logger.debug(t);
		}
		
		writeClass();
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
		
		// TODO: Don't add the boolean $asTask to the list... Or does it even matter?
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
		bodyLocals.addAll(locals);
		
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
	}
	
	private void generateRunRolezMethods() {

		// Generate concrete method
		SootMethod runRolezConcrete = generateRunRolezConcreteMethod();
		
		// Generate "bridge" method
		generateRunRolezObjectMethod(runRolezConcrete);
		
	}
	
	private SootMethod generateRunRolezConcreteMethod() {

		RefType innerClassType = innerClass.getType();
		RefType voidType = RefType.v("java.lang.Void");
		
		SootMethod runRolezConcrete;
		Type returnType = null;
		
		// TODO: Handle primitive types!!! --> This switch may grow large... :-/
		switch (srcMethod.getReturnType().toString()) {
			case("void"):
				returnType = voidType;
				break;
			default:
				returnType = srcMethod.getReturnType();
				break;
		}

		logger.debug("Generating runRolez() : " + returnType.toString());
		
		runRolezConcrete = new SootMethod("runRolez", new ArrayList<Type>(), returnType);
		runRolezConcrete.setModifiers(Modifier.PROTECTED);
		innerClass.addMethod(runRolezConcrete);
		
		// Body of source method
		Body srcMethodBody = srcMethod.retrieveActiveBody();
		logger.debug("SOURCE BEFORE\n" + srcMethodBody);
		
		// Body of created runRolez method is initialized as a copy of the source method's body
		runRolezConcrete.setActiveBody((Body)srcMethodBody.clone());
		Body body = runRolezConcrete.getActiveBody();
		
		// Change type of first local to inner class type
		Chain<Local> locals = body.getLocals();
		locals.getFirst().setType(innerClassType);

		// Transform the units of the source method
		Chain<Unit> units = body.getUnits();
		
		// Refer to fields instead of parameters for the first n identity statements (n = #params + 1)
		int i = 0;
		int n = srcMethod.getParameterCount() + 1;
		Iterator<Unit> unitIter = units.snapshotIterator(); 
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (i == 0) {
				// This ref
				try {
					if (u instanceof IdentityStmt) {
						IdentityStmt idStmt = (IdentityStmt) u;
						if (idStmt.getRightOp() instanceof ThisRef) {
							Value leftOp = idStmt.getLeftOp();
							Unit newUnit = Jimple.v().newIdentityStmt(leftOp, Jimple.v().newThisRef(innerClassType));
							units.insertBefore(newUnit, u);
							units.remove(u);
						} else {
							// Right hand side should always be a this ref
							throw new Exception();
						}
					} else {
						// Should always be an identity statement
						throw new Exception();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (i > 0 && i < n) {
				// Parameter assignments
				try {
					if (u instanceof IdentityStmt) {
						IdentityStmt idStmt = (IdentityStmt) u;
						if (idStmt.getRightOp() instanceof ParameterRef) {
							Value leftOp = idStmt.getLeftOp();
							Unit newUnit = Jimple.v().newAssignStmt(leftOp, Jimple.v().newInstanceFieldRef(locals.getFirst(), innerClass.getFieldByName("val$f"+Integer.toString(i-1)).makeRef()));
							units.insertBefore(newUnit, u);
							units.remove(u);
						} else {
							// Right hand side should always be a parameter ref
							throw new Exception();
						}
					} else {
						// Should always be an identity statement
						throw new Exception();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			else {
				// The other statements except the return statements should work with the initialization from above.
				if (u instanceof ReturnVoidStmt) {
					Unit newReturn = Jimple.v().newReturnStmt(NullConstant.v());
					units.insertBefore(newReturn, u);
					units.remove(u);
				}
			}
			
			i++;
		}		

		logger.debug("GENERATED RUN ROLEZ CONCRETE\n" + runRolezConcrete.getActiveBody().toString());
		logger.debug("SOURCE AFTER\n" + srcMethod.getActiveBody());
		return runRolezConcrete;
	}
	
	private SootMethod generateRunRolezObjectMethod(SootMethod runRolezConcrete) {
		logger.debug("Generating runRolez() : java.lang.Object");
		
		RefType innerClassType = innerClass.getType();
		RefType voidType = RefType.v("java.lang.Void");
		
		SootMethod runRolezObject = new SootMethod("runRolez", new ArrayList<Type>(), objectClass.getType());
		runRolezObject.setModifiers(Modifier.VOLATILE | Modifier.PROTECTED);
		
		JimpleBody body = Jimple.v().newBody(runRolezObject);
		runRolezObject.setActiveBody(body);
		
		Chain<Local> bodyLocals = body.getLocals();
		Local thisLocal = Jimple.v().newLocal("r0", innerClassType);
		bodyLocals.add(thisLocal);
		Local returnLocal = Jimple.v().newLocal("$r1", voidType);
		bodyLocals.add(returnLocal);

		Chain<Unit> units = body.getUnits();
		units.add(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(innerClassType)));
		units.add(Jimple.v().newAssignStmt(returnLocal, Jimple.v().newVirtualInvokeExpr(thisLocal, runRolezConcrete.makeRef())));
		units.add(Jimple.v().newReturnStmt(returnLocal));

		innerClass.addMethod(runRolezObject);
		logger.debug("GENERATED RUN ROLEZ OBJECT\n" + runRolezObject.getActiveBody().toString());	
		return runRolezObject;
	}
	
	private String getClassNameFromMethod() {
		String originMethodName = srcMethod.getName();
		String className = originMethodName.substring(0,1).toUpperCase() + originMethodName.substring(1);
		return targetClass.getName() + "$" + className;
	}
}
