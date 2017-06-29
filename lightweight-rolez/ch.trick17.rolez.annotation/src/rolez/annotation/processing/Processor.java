package rolez.annotation.processing;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Guarded;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;

@SupportedAnnotationTypes({"rolez.annotation.Roleztask","rolez.annotation.Readonly","rolez.annotation.Readwrite","rolez.annotation.Guarded"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor {
	
	private Messager messager;
	private Types types;
	private PrintWriter writer;
	
	Message debugMessage;
	
	// Map containing all rolez tasks found by the processor
	Map<ExecutableElement, Map<String,Role>> rolezTasks = new HashMap<ExecutableElement, Map<String,Role>>();
	
	// Whitelist that contains java standard classes which are allowed to be used in rolez tasks
	public static final String[] WHITELIST = new String[] {
		"java.lang.String",
		"java.lang.Integer",
		"java.lang.Boolean",
		"java.lang.Byte",
		"java.lang.Character",
		"java.lang.Short",
		"java.lang.Long",
		"java.lang.Double",
		"java.lang.Float"
	};
	
	public void init(ProcessingEnvironment env) {
	    super.init(env);
    	messager = env.getMessager();
    	types = env.getTypeUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		processTaskAnnotations(env);
		processGuardedAnnotations(env);
		
		StringBuilder sb = new StringBuilder();
		for (ExecutableElement task : rolezTasks.keySet()) {
			sb.append(task.toString());
			sb.append("\n");
			for (String parameter : rolezTasks.get(task).keySet()) {
				sb.append(parameter);
				sb.append(" ");
				sb.append(rolezTasks.get(task).get(parameter).toString());
				sb.append("\n");
			}
			sb.append("\n\n");
		}
		
		writer.write(sb.toString());
		
		return true;
	}

	/**
	 * Processes classes annotated with the <code>@Guareded</code> annotation, which indicates that
	 * this class will inherit from the Guarded class of the Rolez runtime library.
	 * @param env
	 */
	private void processGuardedAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Guarded.class)) {
			TypeMirror type = annotatedElement.asType();
			TypeMirror supertype = getSupertype(type);
			if(!supertype.toString().equals(Object.class.getName())) {
				Message message = new Message(Kind.ERROR, "The @Guarded annotation is only legal on classes which are direct subtypes of java.lang.Object.", annotatedElement);
				message.print(messager);
			}
		}
	}
	
	/**
	 * Processes the methods that are annotated with <code>@Roleztask</code> and checks whether all 
	 * method parameters have a role declared and are able to be guarded.
	 * @param env
	 */
	private void processTaskAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Roleztask.class)) {
			
			if (!(annotatedElement instanceof ExecutableElement)) {
				Message message = new Message(Kind.ERROR, "Only methods can be annotated as Rolez tasks", annotatedElement);
				message.print(messager);
			}
			
			ExecutableElement task = (ExecutableElement) annotatedElement;
			processTask(task);
		}
	}

	private void processTask(ExecutableElement task) {
		HashMap<String, Role> taskParameters = new HashMap<String, Role>();
		
		boolean hasAsTaskParameter = false;
		for (VariableElement parameter : task.getParameters()) {
			
			// Check if it is $asTask parameter
			if(parameter.toString().equals("$asTask") && isOfBooleanType(parameter)) {
				hasAsTaskParameter = true;
				continue;
			}
			
			// Check type
			if (!isValidParameterType(parameter)) {
				Message message = new Message(Kind.ERROR, "Type is not guarded or on the whitelist.", parameter); 
				message.print(messager);
			}
			
			// Check annotation
			Annotation roAnnotation = parameter.getAnnotation(Readonly.class);
			Annotation rwAnnotation = parameter.getAnnotation(Readwrite.class);
			if (roAnnotation != null) {
				taskParameters.put(parameter.toString(), Role.READONLY);
			} else if (rwAnnotation != null) {
				taskParameters.put(parameter.toString(), Role.READWRITE);
			} else {
				// TODO: Allow pure task parameters by setting no annotation
				//taskParameters.put(parameter.toString(), Role.PURE);
				Message message = new Message(Kind.ERROR, "Method parameters have to be annotated with either @Readonly or @Readwrite", parameter);
				message.print(messager);
			}
		}
		
		if (!hasAsTaskParameter) {
			Message message = new Message(Kind.ERROR, "A task needs a parameter $asTask and it needs to be a final boolean.", task);
			message.print(messager);
		}
		
		// Process the role of "this", which uses the same annotations but on a method declared as roleztask
		Annotation roAnnotation = task.getAnnotation(Readonly.class);
		Annotation rwAnnotation = task.getAnnotation(Readwrite.class);
		if (roAnnotation != null) {
			taskParameters.put("this", Role.READONLY);
		} else if (rwAnnotation != null) {
			taskParameters.put("this", Role.READWRITE);
		} else {
			taskParameters.put("this", Role.PURE);
		}
		
		this.rolezTasks.put(task, taskParameters);
	}

	private boolean isValidParameterType(VariableElement parameter) {
		TypeMirror parameterType = parameter.asType();
		if (isWhitelisted(parameterType)) 
			return true;
		if (isGuardedType(parameterType)) {
			return true;
		}
		return false;
	}
	
	private boolean isWhitelisted(TypeMirror parameterType) {
		for (String element : Processor.WHITELIST) {
			if (element.equals(parameterType.toString())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isGuardedType(TypeMirror type) {
		TypeMirror supertype = getSupertype(type);
		if(supertype.toString().equals(Object.class.getName())) {
			// If the super type is object, then the current type has to be annotated with guarded.
			ParameterTypeVisitor typeVisitor = new ParameterTypeVisitor();
			return type.accept(typeVisitor, types);
		} else if (supertype.toString().equals("rolez.lang.Guarded")) {
			// If the super type is the Guarded class, then we are done.
			return true;
		}
		// If nothing above is true, then we can further climb the inheritance tree to find an annotation
		// or the Guarded class itself.
		return isGuardedType(supertype);
	}
	
	//TODO: This method doesn't work since final modifiers on method parameters are erased after compilation
	//      and therefore element.getModifiers() cannot find the final modifier.
	@SuppressWarnings(value = {"unused"})
	private boolean hasFinalModifier(Element element) {
		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.size() > 1) return false;
		return modifiers.contains(Modifier.FINAL);
	}

	private boolean isOfBooleanType(Element element) {
		return element.asType().toString().equals("boolean");
	}
	
	private TypeMirror getSupertype(TypeMirror type) {
		// Get the super type of the current type (first in list is always a class, further are interfaces)
		List<? extends TypeMirror> supertypes = types.directSupertypes(type);
		return supertypes.get(0);
	}
	
}
